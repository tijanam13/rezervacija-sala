package com.fon.rezervacija_sala.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tip")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NastavaDto.class, name = "NASTAVA"),
    @JsonSubTypes.Type(value = IspitDto.class, name = "ISPIT"),
    @JsonSubTypes.Type(value = ZavrsniRadDto.class, name = "ZAVRSNI_RAD"),
    @JsonSubTypes.Type(value = SastanakDto.class, name = "SASTANAK"),
    @JsonSubTypes.Type(value = DogadjajDto.class, name = "DOGADJAJ")
})
public abstract class SvrhaRezervacijeDto {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}