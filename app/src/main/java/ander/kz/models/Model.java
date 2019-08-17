package ander.kz.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Model {
    @Exclude
    public String uid;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public String  kzSongTitle;
    public String  kzSongSinger;
    public String  kzSongText;
    public String  kzSongytblink;
    public String  kzSongComposerName;
    public String  kzSongGenre;
    public String  kzSongAuthorName;

    public Model(String userId, String username, String kzSongTitle, String kzSongSinger) {
    }
    public Model(){}
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("kzSongTitle", kzSongTitle);
        result.put("kzSongSinger", kzSongSinger);
        return result;
    }
}