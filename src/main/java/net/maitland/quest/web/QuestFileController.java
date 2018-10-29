package net.maitland.quest.web;

import net.maitland.quest.parser.sax.SaxQuestParser;
import net.maitland.quest.persistance.Quest;
import net.maitland.quest.persistance.QuestHeader;
import net.maitland.quest.persistance.QuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
@CrossOrigin
public class QuestFileController {

    QuestRepository questRepository;

    @Autowired
    public QuestFileController(QuestRepository questRepository) {
        this.questRepository = questRepository;
    }

    @GetMapping("/files")
    public @ResponseBody
    List<QuestHeader> listQuestFiles(@RequestParam(name = "title", required = false) String title) throws IOException {

        List<QuestHeader> questList = new ArrayList<>();
        Iterable<QuestHeader> questIt = null;

        if(title == null) {
            questIt = questRepository.findAllHeadersBy();
        }
        else
        {
            questIt = questRepository.findByTitleStartingWithIgnoreCase(title);
        }

        for (QuestHeader q : questIt)
        {
            questList.add(q);
        }

        return questList;
    }

    @GetMapping("/files/{fileId}")
    public @ResponseBody
    String getQuestFile(@PathVariable("fileId") Long fileId) throws IOException {

        Quest quest = questRepository.findOne(fileId);

        return quest.getQuestML();
    }

    @PostMapping("/files")
    public String uploadQuestFile(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            String questXML = streamToString(inputStream);
            String title = validateAndExtractQuestName(questXML);
            Quest quest = new Quest("david.ts.maitland@gmail.com",title, questXML);
            this.questRepository.save(quest);
        } finally {
            if(inputStream != null)
            {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "redirect:/";
    }

    protected String streamToString(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());
    }

    protected String validateAndExtractQuestName(String questXml) throws IOException {

        InputStream is = null;

        try {
            is = new ByteArrayInputStream(questXml.getBytes(StandardCharsets.UTF_8));
            SaxQuestParser qp = new SaxQuestParser();
            net.maitland.quest.model.Quest q = qp.parseQuest(is);
            return q.getAbout().getTitle();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
/*
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleException(IOException exc) {
        return ResponseEntity.badRequest().build();
    }
*/
}
