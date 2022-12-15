package com.twenty80partnership.bibliofyadmin.models;

public class ApplicableTerm {
    String termId,topic;
    int priority;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String codeId) {
        this.termId = codeId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ApplicableTerm() {
    }
}
