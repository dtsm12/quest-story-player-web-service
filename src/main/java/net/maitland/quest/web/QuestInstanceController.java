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
    Map<Long, Quest> quests = new HashMap<>();
    boolean hasBeenCreated = false;

    @Autowired
    public QuestInstanceController(QuestRepository questRepository, QuestParser questParser) {
        this.questRepository = questRepository;
        this.questParser = questParser;
    }

    @RequestMapping(path = "/quests/{questId}/game", method = RequestMethod.GET)
    public GameState newGame(@PathVariable Long questId) throws QuestStateException, ChoiceNotPossibleException {
        Quest quest = getQuest(questId);
        Game game = quest.newGameInstance();
        game.setChoiceId(QuestStation.START_STATION_ID);
        GameState gameState = getGameState(quest, game);
        return gameState;
    }

    @RequestMapping(path = "/quests/{questId}/game", method = RequestMethod.PUT)
    public GameState gameChoice(@PathVariable Long questId, @RequestBody String content) throws Exception {
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

    protected Quest getQuest(Long questId) throws QuestStateException {

        Quest q = null;
        try {

            q = this.quests.get(questId);
            if(q == null) {
                net.maitland.quest.persistance.Quest iqs = this.questRepository.findOne(questId);
                q = this.questParser.parseQuest(new ByteArrayInputStream(iqs.getQuestML().getBytes(StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            throw new QuestStateException("Error getting Quest from repository", e);
        }

        return q;
    }
}
