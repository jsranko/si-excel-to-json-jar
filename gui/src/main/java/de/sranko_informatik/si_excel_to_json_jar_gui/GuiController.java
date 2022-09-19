package de.sranko_informatik.si_excel_to_json_jar_gui;

import de.sranko_informatik.si_excel_to_json_jar_core.ExcelParser;
import de.sranko_informatik.si_excel_to_json_jar_core.FileService;
import de.sranko_informatik.si_excel_to_json_jar_core.TainasResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

@Controller
public class GuiController {

    @Value("${client.ssl.trust-store}")
    private String trustStore;

    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;

    FileService fileService;

    @GetMapping("/")
    public String index(@RequestParam(name = "callback") String callbackUrl,
                        Model model) {
        model.addAttribute("callbackUrl", callbackUrl);
        return "upload";
    }

    @GetMapping("/uploaded")
    public String uploaded() {
        return "uploaded";
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam(name = "file") MultipartFile file,
                             @RequestParam(name = "callback") String callbackUrl,
                             @RequestParam(name = "clientInfo") String clientInfo,
                             RedirectAttributes redirectAttributes) {

        Logger logger = LoggerFactory.getLogger(GuiController.class);

        JSONObject callbackData = null;
        TainasResponse response = null;
        String msg = null;
        fileService = new FileService();
        try {
            callbackData = fileService.parseFile(file);
            logger.debug(callbackData.toString());
            response = fileService.sendData(callbackData, new JSONObject(clientInfo),callbackUrl, trustStore, trustStorePassword);

        } catch ( NullPointerException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response = new TainasResponse("Exception", "NullPointerException", sw.toString().substring(1, 256).concat(" ..."), "N/A");

        }catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response = new TainasResponse("Error", "IOException", sw.toString().substring(1, 256).concat(" ..."), "N/A");

        }
        logger.debug(response.toString());
        redirectAttributes.addFlashAttribute("status", response.getStatus());
        redirectAttributes.addFlashAttribute("jobid", response.getJobid());
        redirectAttributes.addFlashAttribute("messageId", response.getMessageId());
        redirectAttributes.addFlashAttribute("messageText", response.getMessageText());

        return "redirect:/uploaded";
    }

}
