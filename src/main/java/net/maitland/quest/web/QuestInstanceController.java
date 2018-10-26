package net.maitland.quest.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.maitland.quest.model.*;
import net.maitland.quest.model.Quest;
import net.maitland.quest.parser.QuestParser;
import net.maitland.quest.parser.sax.SaxQuestParser;
import net.maitland.quest.persistance.*;
import net.maitland.quest.player.ChoiceNotPossibleException;
import net.maitland.quest.player.ConsolePlayer;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Created by David on 13/01/2017.
 */
@RestController
public class QuestInstanceController {

    QuestRepository questRepository;
    QuestParser questParser;
    Map<Integer, Quest> quests = new HashMap<>();
    boolean hasBeenCreated = false;

    @Autowired
    public QuestInstanceController(QuestRepository questRepository, QuestParser questParser) {
        this.questRepository = questRepository;
        this.questParser = questParser;
    }

    @RequestMapping(path = "/quests/{questId}/game", method = RequestMethod.GET)
    public GameState newGame(@PathVariable Integer questId) throws QuestStateException, ChoiceNotPossibleException {
        Quest quest = getQuest(questId);
        Game game = quest.newGameInstance();
        game.setChoiceId(QuestStation.START_STATION_ID);
        GameState gameState = getGameState(quest, game);
        return gameState;
    }

    @RequestMapping(path = "/quests/{questId}/game", method = RequestMethod.PUT)
    public GameState gameChoice(@PathVariable Integer questId, @RequestBody String content) throws Exception {
        Quest quest = getQuest(questId);
        Map<String, Object> gameData = (new ObjectMapper()).readValue(content, Map.class);
        Game game = Game.fromCollectionStructure(gameData);
        GameState gameState = getGameState(quest, game);
        return gameState;
    }

    protected GameState getGameState(Quest quest, Game game) throws ChoiceNotPossibleException {
        GameStation gameStation = quest.getNextStation(game);
        GameState gameState = new GameState();
        gameState.setGame(game);
        gameState.setGameStation(gameStation);
        return gameState;
    }

    protected Quest getQuest(int questId) throws QuestStateException {

        Quest q = null;
        try {

            if(hasBeenCreated == false)
            {
                String author = "David Maitland";
                String title = "Chance Persistance";
                String questML = getQuestML();
                net.maitland.quest.persistance.Quest iq = new net.maitland.quest.persistance.Quest(null, author, title, questML);
                this.questRepository.save(iq);
                this.hasBeenCreated = true;
            }

            q = this.quests.get(questId);
            if(q == null) {
                net.maitland.quest.persistance.Quest iqs = this.questRepository.findAll().iterator().next();
                q = this.questParser.parseQuest(new ByteArrayInputStream(iqs.getQuestML().getBytes(StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            throw new QuestStateException("Error getting Quest from repository", e);
        }

        return q;
    }

    protected String getQuestML() {

        String questML = "";
        InputStream is = null;

        try {
            is = ConsolePlayer.class.getClassLoader().getResourceAsStream("chance-quest.xml");
            questML = IOUtils.toString(is, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return questML;
    }
/*
    protected Quest getQuest(int questId) {
        Quest q = this.quests.get(questId);

        if(q == null) {
            InputStream is = null;

            try {
                is = ConsolePlayer.class.getClassLoader().getResourceAsStream("chance-quest.xml");
                SaxQuestParser qp = new SaxQuestParser();
                q = qp.parseQuest(is);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            this.quests.put(questId, q);
        }
        return q;
    }*/
}
