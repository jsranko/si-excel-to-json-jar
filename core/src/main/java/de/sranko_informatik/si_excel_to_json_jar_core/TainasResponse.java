package de.sranko_informatik.si_excel_to_json_jar_core;

public class TainasResponse {
    public String jobid;
    public String status;
    public String messageId;
    public String messageText;

    public TainasResponse() {

    }

    public TainasResponse(String status, String messageId, String messageText, String jobid) {
        this.jobid = jobid;
        this.status = status;
        this.messageId = messageId;
        this.messageText = messageText;

    }

    public boolean isError(){
        if (status == "Error") return true;
        else return false;
    }

    public String getStatus() {
        return status;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return String.format("jobid: %s, status: %s, messageId: %s, messageText: %s", getJobid(), getStatus(), getMessageId(), getMessageText());
    }

    public String getJobid() {
        return jobid;
    }
}
