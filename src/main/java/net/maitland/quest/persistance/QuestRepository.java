package net.maitland.quest.persistance;

import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by David on 06/04/2017.
 */
@Repository
public interface QuestRepository extends CrudRepository<Quest, Long> {

    List<Quest> findByTitle(String title);
    List<Quest> findByAuthor(String author);
}
