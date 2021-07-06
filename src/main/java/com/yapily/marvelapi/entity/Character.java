package com.yapily.marvelapi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;

@Entity
@ApiModel
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ApiModelProperty(notes = "Db generated ID")
    private int id;
    @ApiModelProperty(notes = "Official Marvel API ID")
    private int marvelid;
    @ApiModelProperty(notes = "Character Name")
    private String name;
    @ApiModelProperty(notes = "Character Description")
    private String description;
    private String description_overflow;

    @Embedded
    @ApiModelProperty(notes = "Character Image")
    private Thumbnail thumbnail;

    public Character() {

    }

    public Character(int marvel_id, String name, String description, String description_overflow, Thumbnail thumbnail) {
        this.marvelid = marvel_id;
        this.name = name;
        this.description = description;
        this.description_overflow = description_overflow;
        this.thumbnail = thumbnail;
    }

    public int getMarvelid() {
        return marvelid;
    }

    public void setMarvelid(int marvelid) {
        this.marvelid = marvelid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription_overflow() {
        return description_overflow;
    }

    public void setDescription_overflow(String description_overflow) {
        this.description_overflow = description_overflow;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }
}
