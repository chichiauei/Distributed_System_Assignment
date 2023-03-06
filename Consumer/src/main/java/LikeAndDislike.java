import java.util.concurrent.atomic.AtomicInteger;

public class LikeAndDislike {
    protected AtomicInteger likes;
    protected AtomicInteger dislikes;

    public LikeAndDislike(AtomicInteger likes, AtomicInteger dislikes) {
        this.likes = likes;
        this.dislikes = dislikes;
    }

    public AtomicInteger getLikes() {
        return likes;
    }

    public void setLikes(AtomicInteger likes) {
        this.likes = likes;
    }

    public AtomicInteger getDislikes() {
        return dislikes;
    }

    public void setDislikes(AtomicInteger dislikes) {
        this.dislikes = dislikes;
    }

    public void incrementLike(){
        this.likes.getAndIncrement();
    }

    public void incrementDislike(){
        this.dislikes.getAndIncrement();
    }
}
