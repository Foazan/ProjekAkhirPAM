package com.example.onlyart;

public class Work {
    public String imageUrl;
    public String tags;
    public  String desc;
    public  String uid;
    public  String title;
    public  boolean ai_generated;

    public Work() {}

    public Work(String imageUrl, String uid, String title, String desc, String tags, boolean ai_generated) {
        this.imageUrl = imageUrl;
        this.uid = uid;
        this.title = title;
        this.desc = desc;
        this.tags = tags;
        this.ai_generated = ai_generated;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getTags() {
        return tags;
    }

    public String getDesc() {
        return desc;
    }

    public String getUid() {
        return uid;
    }

    public String getTitle(){
        return title;
    }

    public boolean getAiGenerated(){
        return ai_generated;
    }
}
