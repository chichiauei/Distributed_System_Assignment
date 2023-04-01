import java.util.List;

public interface SwipeDao {
    int getNumLikesForUser(String userId);
    int getNumDislikesForUser(String userId);
    List<String> getMatchesForUser(String userId);
}