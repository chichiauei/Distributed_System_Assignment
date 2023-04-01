import java.sql.*;
import org.apache.commons.dbcp2.*;
public class ConsumerDao {
    private static BasicDataSource dataSource;

    public ConsumerDao() {
        dataSource = DBCPDataSource.getDataSource();
    }


    public void createSwipeData(swipeData swipe){
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO SwipeData (swiper, swipee, swipe) " +
                "VALUES (?,?,?)";
        try {
            conn = dataSource.getConnection();
            preparedStatement = conn.prepareStatement(insertQueryStatement);
            preparedStatement.setString(1, swipe.getSwiper());
            preparedStatement.setString(2, swipe.getSwipee());
            preparedStatement.setString(3, swipe.getSwipe());
            // execute insert SQL statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}