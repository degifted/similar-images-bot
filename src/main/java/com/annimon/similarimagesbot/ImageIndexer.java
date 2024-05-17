package com.annimon.similarimagesbot;

import com.annimon.similarimagesbot.data.ImageResult;
import com.annimon.similarimagesbot.data.Post;
import com.annimon.similarimagesbot.data.SimilarImagesInfo;
import com.github.kilianB.hashAlgorithms.DifferenceHash;
import com.github.kilianB.hashAlgorithms.PerceptiveHash;
import java.awt.image.BufferedImage;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static com.github.kilianB.hashAlgorithms.DifferenceHash.Precision;
public class ImageIndexer {

    private final Map<Long, SimilarImagesH2DatabaseMatcher> databases = new HashMap<>(5);
    private final DifferenceHash differenceHash = new DifferenceHash(32, Precision.Double);
    private final PerceptiveHash perceptiveHash = new PerceptiveHash(32);

    public SimilarImagesInfo processImage(Post originalPost, BufferedImage image)
            throws SQLException {
        final Long channelId = originalPost.getChannelId();
        final String uniqueId = originalPost.getMessageId().toString() + "_" + channelId.toString();
        final var db = getDatabaseForChannel(new Long(0));
        final List<ImageResult> results = db.getMatchingImages(image)
                .stream()
                .map(r -> {
                    String[] data = r.value.split("_");
                    final var similarPost = new Post(Long.parseLong(data[1]), Integer.parseInt(data[0]));
                    return new ImageResult(similarPost, r.distance);
                })
                //.filter(r -> !r.getPost().getChannelId().equals(channelId))
                .filter(r -> r.getDistance() < 5.0)
                .filter(r -> !r.isSamePost(originalPost))
                .collect(Collectors.toList());
        if (results.isEmpty()) db.addImage(uniqueId, image);

        //return new SimilarImagesInfo(originalPost, results);
        return new SimilarImagesInfo(originalPost, results.stream().filter(r -> !r.getPost().getChannelId().equals(channelId)).collect(Collectors.toList()));
    }


    public void deleteImage(Long channelId, Integer messageId) throws SQLException {
        final String uniqueId = messageId.toString();
        final var db = getDatabaseForChannel(channelId);
        db.removeImage(uniqueId);
    }

    private SimilarImagesH2DatabaseMatcher getDatabaseForChannel(Long channelId) throws SQLException {
        var db = databases.get(channelId);
        if (db != null) {
            return db;
        }
        var jdbcUrl = "jdbc:h2:./imagesdb_" + channelId;
        var conn = DriverManager.getConnection(jdbcUrl, "root", "");
        db = new SimilarImagesH2DatabaseMatcher(conn);
        db.addHashingAlgorithm(differenceHash, 0.4);
        db.addHashingAlgorithm(perceptiveHash, 0.2);
        databases.put(channelId, db);
        return db;
    }
}
