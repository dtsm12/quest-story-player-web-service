package net.maitland.quest.web;

import net.maitland.quest.parser.QuestParser;
import net.maitland.quest.parser.sax.SaxQuestParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by David on 07/04/2017.
 */
@Configuration
public class QuestParserConfiguration {
    @Bean
    public QuestParser serviceFactory() {
        return new SaxQuestParser();
    }
}
