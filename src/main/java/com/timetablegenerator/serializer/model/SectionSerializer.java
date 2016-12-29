package com.timetablegenerator.serializer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.serializer.model.period.OneTimePeriodSerializer;
import com.timetablegenerator.serializer.model.period.RepeatingPeriodSerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionSerializer implements Serializer<Section> {

    @JsonProperty("id") private String sectionId = null;
    @JsonProperty("serial") private String serial = null;
    @JsonProperty("groupId") private String groupId = null;

    @JsonProperty("waitingList") private Boolean waitingList = null;
    @JsonProperty("waitingNum") private Integer waitingNum = null;
    @JsonProperty("maxWaitingNum") private Integer maxmWaitingNum = null;

    @JsonProperty("full") private Boolean full = null;
    @JsonProperty("enrolled") private Integer enrolled = null;
    @JsonProperty("maxEnrollment") private Integer maximumEnrolled = null;

    @JsonProperty("online") private Boolean online = null;
    @JsonProperty("cancelled") private Boolean cancelled = null;

    @JsonProperty("notes") private List<String> notes = null;
    @JsonProperty("repeatingPeriods") private List<Serializer<RepeatingPeriod>> repeatingPeriods = null;
    @JsonProperty("oneTimePeriods") private List<Serializer<OneTimePeriod>> oneTimePeriods = null;

    @Override
    public Serializer<Section> fromInstance(Section instance) {

        this.sectionId = instance.getSectionId();
        this.serial = instance.getSerialNumber().orElse(null);
        this.groupId = instance.getGroupId().orElse(null);

        this.waitingList = instance.hasWaitingList().orElse(null);
        this.waitingNum = instance.getWaiting().orElse(null);
        this.maxmWaitingNum = instance.getMaxWaiting().orElse(null);

        this.full = instance.isFull().orElse(null);
        this.enrolled = instance.getEnrollment().orElse(null);
        this.maximumEnrolled = instance.getMaxEnrollment().orElse(null);

        this.online = instance.isOnline().orElse(null);
        this.cancelled = instance.isCancelled().orElse(null);

        this.notes = instance.getNotes();
        this.repeatingPeriods = instance.getRepeatingPeriods().stream()
                .map(rp -> new RepeatingPeriodSerializer().fromInstance(rp))
                .collect(Collectors.toList());
        this.oneTimePeriods = instance.getOneTimePeriods().stream()
                .map(otp -> new OneTimePeriodSerializer().fromInstance(otp))
                .collect(Collectors.toList());
        return this;
    }

    @Override
    public Section toInstance(SerializerContext context) {

        Section instance = Section.of(this.sectionId);

        Optional.ofNullable(this.serial).ifPresent(instance::setSerialNumber);
        Optional.ofNullable(this.groupId).ifPresent(instance::setGroupId);

        Optional.ofNullable(this.waitingList).ifPresent(instance::setWaitingList);
        Optional.ofNullable(this.waitingNum).ifPresent(instance::setWaiting);
        Optional.ofNullable(this.maxmWaitingNum).ifPresent(instance::setMaximumWaiting);

        Optional.ofNullable(this.full).ifPresent(instance::setFull);
        Optional.ofNullable(this.enrolled).ifPresent(instance::setEnrollment);
        Optional.ofNullable(this.maximumEnrolled).ifPresent(instance::setMaximumEnrollment);

        Optional.ofNullable(this.online).ifPresent(instance::setOnline);
        Optional.ofNullable(this.cancelled).ifPresent(instance::setCancelled);

        instance.addNotes(this.notes);

        this.repeatingPeriods.stream()
                .map(rps -> rps.toInstance(context))
                .forEach(instance::addPeriod);

        this.oneTimePeriods.stream()
                .map(otps -> otps.toInstance(context))
                .forEach(instance::addPeriod);

        return instance;
    }
}