package de.sranko_informatik.si_excel_to_json_jar_gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.sranko_informatik.si_excel_to_json_jar_core.ActionData;
import de.sranko_informatik.si_excel_to_json_jar_core.ActionDataSheet;
import de.sranko_informatik.si_excel_to_json_jar_core.FileService;
import de.sranko_informatik.si_excel_to_json_jar_core.TainasResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Objects;

@Controller
public class GuiController {

    @Value("${client.ssl.trust-store}")
    private String trustStore;

    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;

    FileService fileService;

    @GetMapping("/")
    public String index(@RequestParam(name = "d") String base64String,
                        Model model) {
        // Base64 dekodieren um URL und actionDaten zu ermitteln
        String callbackData = new String(Base64.getDecoder().decode(base64String));

        //JSON String parsen
        ObjectMapper objectMapper = new ObjectMapper();
        ActionData actionData = null;
        try {
            actionData = objectMapper.readValue(callbackData, ActionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("actionData", actionData.getActionData());
        model.addAttribute("callbackUrl", actionData.getUrl());

        return "upload";
    }

    @GetMapping("/uploaded")
    public String uploaded() {
        return "uploaded";
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam(name = "file") MultipartFile file,
                             @RequestParam(name = "callback") String callbackUrl,
                             @RequestParam(name = "actionData") String actionData,
                             @RequestParam(name = "clientInfo") String clientInfo,
                             RedirectAttributes redirectAttributes) {

        Logger logger = LoggerFactory.getLogger(GuiController.class);

        JSONObject callbackData = null;
        TainasResponse response = null;
        String msg = null;
        ActionDataSheet actionDataSheet = null;
        fileService = new FileService();

        if (!Objects.isNull(actionData) & actionData.length() != 0) {
            logger.debug(String.format("actionData ubergeben: %s,", actionData));

            ObjectMapper mapper = new ObjectMapper();
            try {
                actionDataSheet = mapper.readValue(actionData, ActionDataSheet.class);
            } catch (JsonProcessingException  e) {
                e.printStackTrace();
            }

        } else {
            logger.debug("Keine actionData ubergeben");
        }

        try {
            callbackData = fileService.parseFile(file, actionDataSheet);
            logger.debug(callbackData.toString());
            response = fileService.sendData(callbackData, new JSONObject(clientInfo), callbackUrl, trustStore, trustStorePassword);

        } catch ( NullPointerException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response = new TainasResponse("Exception", "NullPointerException", sw.toString().substring(0, 256).concat(" ..."), "N/A");

        }catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response = new TainasResponse("Error", "IOException", sw.toString().substring(0, 256).concat(" ..."), "N/A");

        }
        logger.debug(response.toString());
        redirectAttributes.addFlashAttribute("status", response.getStatus());
        redirectAttributes.addFlashAttribute("jobid", response.getJobid());
        redirectAttributes.addFlashAttribute("messageId", response.getMessageId());
        redirectAttributes.addFlashAttribute("messageText", response.getMessageText());

        return "redirect:/uploaded";
    }

}
