import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SwipeDaoImpl {
    private static BasicDataSource dataSource;

    public SwipeDaoImpl() {
        dataSource = DBCPDataSource.getDataSource();
    }


    public int getNumLikesForUser(String userId) {
        int numLikes = 0;
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            String query = "SELECT COUNT(*) AS num_likes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='right'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                numLikes = resultSet.getInt("num_likes");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numLikes;
    }


    public int getNumDislikesForUser(String userId) {
        int numDislikes = 0;
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            String query = "SELECT COUNT(*) AS num_dislikes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='left'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                numDislikes = resultSet.getInt("num_dislikes");
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numDislikes;
    }


    public List<String> getMatchesForUser(String userId) {
        // Query database to retrieve up to 100 matches for the user
        // Return as list of strings
        // You can use the SELECT statement with a JOIN operation to retrieve matches
        List<String> matches = new ArrayList<String>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            String query = "SELECT swipee FROM SwipeData WHERE swiper='" + userId + "' AND swipe = 'right' LIMIT 100";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                matches.add(resultSet.getString("swipee"));
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return matches;
    }
}










//import org.apache.commons.dbcp2.BasicDataSource;
//
//        import java.sql.*;
//
//public class SwipeDaoImpl implements SwipeDao {
//    private static BasicDataSource dataSource;
//
//    public SwipeDaoImpl() {
//        dataSource = DBCPDataSource.getDataSource();
//    }
//
//    @Override
//    public int getNumLikesForUser(String userId) {
//        int numLikes = 0;
//        try (Connection connection = dataSource.getConnection()) {
//            Statement statement = connection.createStatement();
//            String query = "SELECT COUNT(*) AS num_likes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='right'";
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                numLikes = resultSet.getInt("num_likes");
//            }
//            statement.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return numLikes;
//    }
//
//    @Override
//    public int getNumDislikesForUser(String userId) {
//        int numDislikes = 0;
//        try (Connection connection = dataSource.getConnection()) {
//            Statement statement = connection.createStatement();
//            String query = "SELECT COUNT(*) AS num_dislikes FROM SwipeData WHERE swiper='" + userId + "' AND swipe='left'";
//            ResultSet resultSet = statement.executeQuery(query);
//            if (resultSet.next()) {
//                numDislikes = resultSet.getInt("num_dislikes");
//            }
//            statement.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return numDislikes;
//    }
//}