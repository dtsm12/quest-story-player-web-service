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

    @Column(columnDefinition="CLOB NOT NULL")
    private String questML;

    public Quest() {

    }

    public Quest(Long id, String author, String title, String questML) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.questML = questML;
    }

    @Override
    public String toString() {
        return String.format(
                "Quest[id=%d, author='%s', title='%s']",
                id, author, title);
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

    public String getQuestML() {
        return questML;
    }
}
