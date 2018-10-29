package net.maitland.quest.persistance;

import javax.persistence.*;

/**
 * Created by David on 06/04/2017.
 */
@Entity
public class Quest {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String author;
    private String title;

    @Lob
    private String questML;

    public Quest() {

    }

    public Quest(Long id, String author, String title, String questML) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.questML = questML;
    }

    public Quest(String author, String title, String questML) {
        this(null, author, title, questML);
    }

    public String getQuestML() {
        return questML;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return String.format(
                "Quest[id=%d, author='%s', title='%s']",
                id, author, title);
    }
}
