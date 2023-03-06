public class swipeData {

    protected String swipe;
    protected String swiper;
    protected String swipee;
    protected String comment;


    public swipeData( String swipe,String swiper, String swipee, String comment) {
        this.swipe = swipe;
        this.swiper = swiper;
        this.swipee = swipee;
        this.comment = comment;
    }

    public String getSwipe() {
        return swipe;
    }

    public void setSwipe(String swipe) {
        this.swipe = swipe;
    }

    public String getSwiper() {
        return swiper;
    }

    public void setSwiper(String swiper) {


        this.swiper = swiper;

    }

    public String getSwipee() {
        return swipee;
    }

    public void setSwipee(String swipee) {

        this.swipee = swipee;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


}
