package net.maitland.quest.web;

import net.maitland.quest.model.*;
import net.maitland.quest.parser.sax.SaxQuestParser;
import net.maitland.quest.player.ChoiceNotPossibleException;
import net.maitland.quest.player.ConsolePlayer;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

/**
 * Created by David on 13/01/2017.
 */
@RestController
public class QuestInstanceController {

    Map<Integer, Quest> quests = new HashMap<>();

    @RequestMapping(path = "/quest/{questId}/game", method = RequestMethod.GET)
    public GameState newGame(@PathVariable Integer questId) throws QuestStateException, ChoiceNotPossibleException {
        Quest quest = getQuest(questId);
        Game game = quest.newGameInstance();
        game.setChoiceId(QuestStation.START_STATION_ID);
        GameState gameState = getGameState(quest, game);
        return gameState;
    }

    @RequestMapping(path = "/quest/{questId}/game", method = RequestMethod.PUT)
    public GameState gameChoice(@PathVariable Integer questId, @RequestBody Game game) throws QuestStateException, ChoiceNotPossibleException {
        Quest quest = getQuest(questId);
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
    }
}
