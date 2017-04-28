package net.maitland.quest.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.maitland.quest.model.About;
import net.maitland.quest.model.Game;
import net.maitland.quest.model.attribute.Attribute;
import net.maitland.quest.model.attribute.NumberAttribute;
import net.maitland.quest.model.attribute.StateAttribute;
import net.maitland.quest.model.attribute.StringAttribute;
import net.maitland.quest.parser.QuestParser;
import net.maitland.quest.persistance.Quest;
import net.maitland.quest.persistance.QuestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(QuestInstanceController.class)
public class QuestInstanceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private QuestRepository questRepository;

    @MockBean
    private QuestParser questParser;

    @MockBean
    private Quest pQuest;

    @MockBean
    private net.maitland.quest.model.Quest quest;

    @Test
    public void newGame() throws Exception {

        given(this.questRepository.findAll())
                .willReturn(Collections.singletonList(pQuest));

        given(this.pQuest.getQuestML()).willReturn("");

        given(this.questParser.parseQuest(any(InputStream.class))).willReturn(quest);

        given(this.quest.newGameInstance()).willReturn(new Game());

        this.mvc.perform(get("/quest/1/game"));

        verify(quest).newGameInstance();
    }

    @Test
    public void gameChoice() throws Exception {
        Game game = new Game(new About("title1", "author1"));
        game.getGameQuest().setIntro("intro1");
        game.setChoiceIndex(1);
        game.setChoiceId("choice1");
        game.getQuestPath().push("start");
        game.getQuestPath().push("station1");
        game.getQuestPath().push("station2");

        List<Attribute> attributes = new ArrayList();
        attributes.add(new NumberAttribute("attribute1", "0", "-1", "100"));
        attributes.add(new StringAttribute("attribute2", "abc"));
        attributes.add(new StateAttribute("attribute3", "true"));
        game.updateState(attributes);

        ObjectMapper objectMapper = new ObjectMapper();

        given(this.questRepository.findAll())
                .willReturn(Collections.singletonList(pQuest));

        given(this.pQuest.getQuestML()).willReturn("");

        given(this.questParser.parseQuest(any(InputStream.class))).willReturn(quest);

        this.mvc.perform(put("/quest/1/game")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(game)));

        verify(quest).getNextStation(any(Game.class));
    }

}