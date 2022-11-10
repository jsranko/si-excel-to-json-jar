package de.sranko_informatik.si_excel_to_json_jar_core;

public class ActionData {
    private String url;
    private String actionData;

    public ActionData() {
    }

    public ActionData(String url, String actionData) {
        this.url = url;
        this.actionData = actionData;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    @Override
    public String toString() {
        return "ActionData{" +
                "url='" + url + '\'' +
                ", actionDataSheet=" + actionData +
                '}';
    }
}
