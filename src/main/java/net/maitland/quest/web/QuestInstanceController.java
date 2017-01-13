package net.maitland.quest.web;

import net.maitland.quest.SaxQuestParser;
import net.maitland.quest.model.Quest;
import net.maitland.quest.model.QuestStateStation;
import net.maitland.quest.player.ChoiceNotPossibleException;
import net.maitland.quest.player.ConsolePlayer;
import net.maitland.quest.player.QuestInstance;
import net.maitland.quest.player.QuestStateException;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.fail;

/**
 * Created by David on 13/01/2017.
 */
@RestController
public class QuestInstanceController {

    private static final String QUEST_INSTANCE = "QUEST_INSTANCE";

    @RequestMapping("/beginQuest")
    public QuestStateStation beginQuest(HttpServletRequest request) throws QuestStateException, ChoiceNotPossibleException {
        QuestInstance questInstance = getQuestInstance(request, true);
        return questInstance.getNextStation(null);
    }

    @RequestMapping("/questChoice")
    public QuestStateStation beginQuest(HttpServletRequest request, @RequestParam(value = "choiceIndex") int choiceIndex) throws QuestStateException, ChoiceNotPossibleException {
        QuestInstance questInstance = getQuestInstance(request);
        return questInstance.getNextStation(choiceIndex);
    }

    protected QuestInstance getQuestInstance(HttpServletRequest request)
    {
        return getQuestInstance(request, false);
    }

    protected QuestInstance getQuestInstance(HttpServletRequest request, boolean recreate)
    {
        HttpSession session = request.getSession(true);
        QuestInstance questInstance = (QuestInstance) session.getAttribute(QUEST_INSTANCE);

        if(questInstance == null || recreate)
        {
            questInstance = new QuestInstance(getQuest());
            session.setAttribute(QUEST_INSTANCE, questInstance);
        }

        return questInstance;
    }

    protected Quest getQuest() {
        Quest q = null;
        InputStream is = null;

        try {
            is = ConsolePlayer.class.getClassLoader().getResourceAsStream("bargames-quest.xml");
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
        return q;
    }
}
