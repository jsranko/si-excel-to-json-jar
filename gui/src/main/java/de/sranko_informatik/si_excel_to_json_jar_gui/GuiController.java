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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        if (base64String.isEmpty()) {
            model.addAttribute("message", "Is Job CCSID not *HEX?");
            return "error";
        }
        String callbackData = null;
        try {
            callbackData = new String(Base64.getDecoder().decode(base64String));
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "URL-Data not valid (incorrect BAS64 data)");
            return "error";
        }


        //JSON String parsen
        ObjectMapper objectMapper = new ObjectMapper();
        ActionData actionData = null;
        try {
            actionData = objectMapper.readValue(callbackData, ActionData.class);
        } catch (IOException e) {
            model.addAttribute("message", "ActionData not valid.");
            return "error";
        }

        model.addAttribute("actionData", actionData.getActionData());
        model.addAttribute("callbackUrl", actionData.getUrl());

        return "upload";
    }

    @GetMapping("/2")
    public String index2(@RequestParam(name = "d") String base64String,
                        Model model) {
        // Base64 dekodieren um URL und actionDaten zu ermitteln
        if (base64String.isEmpty()) {
            model.addAttribute("message", "Is Job CCSID not *HEX?");
            return "index";
        }
        String callbackData = null;
        try {
            callbackData = new String(Base64.getDecoder().decode(base64String));
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "URL-Data not valid (incorrect BAS64 data)");
            return "index";
        }

        model.addAttribute("callbackData", callbackData);

        return "index";
    }

    @GetMapping("/uploaded")
    public String uploaded() {
        return "uploaded";
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<TainasResponse> uploadFile(@RequestParam(name = "file") MultipartFile file,
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
            logger.debug("Keine actionData übergeben");
        }

        try {
            callbackData = fileService.parseFile(file, actionDataSheet);
            logger.debug(callbackData.toString());
        } catch ( NullPointerException | IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TainasResponse("Sorry something went wrong :-)", e.getMessage(), sw.toString().substring(0, 256).concat(" ..."), "Error occurred while parsing the file."));
        }

        try {
            response = fileService.sendData(callbackData, new JSONObject(clientInfo), callbackUrl, trustStore, trustStorePassword);
            logger.debug(response.toString());
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TainasResponse("Sorry something went wrong :-)", "IOException", sw.toString().substring(0, 256).concat(" ..."), "An error occurred when sending the data"));
        }

//        logger.debug(response.toString());
//        redirectAttributes.addFlashAttribute("status", response.getStatus());
//        redirectAttributes.addFlashAttribute("jobid", response.getJobid());
//        redirectAttributes.addFlashAttribute("messageId", response.getMessageId());
//        redirectAttributes.addFlashAttribute("messageText", response.getMessageText());

        return ResponseEntity.ok(response);
    }

}
