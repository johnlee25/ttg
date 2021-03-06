package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@Accessors(chain = true)
public class Course implements Diffable<Course> {

    private static final String I = Settings.getIndent();

    // Required data.
    @NonNull @Getter private final School school;
    @NonNull @Getter private final Term term;
    @NonNull @Getter private final Department department;
    @NonNull @Getter private final String code;
    @NonNull @Getter private final String name;

    // Optional data.
    @NonNull @Setter private String description = null;
    @NonNull @Setter private Double credits = null;
    private final List<String> notes = new ArrayList<>();

    private final Map<String, Course> crossListings = new HashMap<>();
    private final Map<String, Course> prerequisites = new HashMap<>();
    private final Map<String, Course> antirequisites = new HashMap<>();
    private final Map<String, Course> corequisites = new HashMap<>();

    private final Map<String, SectionType> sectionTypes = new HashMap<>();

    public String getUniqueId(){
        return this.department.getCode() + this.code + this.term.getTermDefinition().getCode();
    }

    private void checkRelationalIntegrity(Map<String, Course> collection, Course course) {
        if (course == this) {
            throw new IllegalArgumentException("A course cannot be related to itself");
        }
        String courseId = course.getUniqueId();
        boolean ok = (collection == this.prerequisites || !this.prerequisites.keySet().contains(courseId)) &&
                (collection == this.antirequisites || !this.antirequisites.keySet().contains(courseId)) &&
                (collection == this.crossListings || !this.crossListings.keySet().contains(courseId)) &&
                (collection == this.corequisites || !this.corequisites.keySet().contains(courseId));
        if (!ok) {
            throw new IllegalArgumentException(
                    String.format("Course %s cannot be under multiple relation categories", course.getUniqueId()));
        }
    }

    public Course addPrerequisite(Course c) {
        this.checkRelationalIntegrity(this.prerequisites, c);
        this.prerequisites.put(c.getUniqueId(), c);
        return this;
    }

    public Course addAntirequisite(Course c) {
        this.checkRelationalIntegrity(this.antirequisites, c);
        this.antirequisites.put(c.getUniqueId(), c);
        return this;
    }

    public Course addCorequesite(Course c) {
        this.checkRelationalIntegrity(this.corequisites, c);
        this.corequisites.put(c.getUniqueId(), c);
        return this;
    }

    public Course addCrossListing(Course c) {
        this.checkRelationalIntegrity(this.crossListings, c);
        this.crossListings.put(c.getUniqueId(), c);
        return this;
    }

    public Course addNotes(@NonNull Collection<String> notes) {
        this.notes.addAll(notes);
        return this;
    }

    public Course addNotes(@NonNull String... notes) {
        Collections.addAll(this.notes, notes);
        return this;
    }

    public Optional<Double> getCredits() {
        return Optional.ofNullable(this.credits);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(this.notes);
    }

    public Map<String, Course> getCrossListings() {
        return Collections.unmodifiableMap(this.crossListings);
    }

    public Map<String, Course> getPrerequisites() {
        return Collections.unmodifiableMap(this.prerequisites);
    }

    public Map<String, Course> getAntirequisites() {
        return Collections.unmodifiableMap(this.antirequisites);
    }

    public Map<String, Course> getCorequisites() {
        return Collections.unmodifiableMap(this.corequisites);
    }

    public Collection<String> getSectionTypes() {
        return this.sectionTypes.keySet();
    }

    public Optional<SectionType> getSectionType(String type) {
        return this.sectionTypes.containsKey(type) ? Optional.of(this.sectionTypes.get(type)) : Optional.empty();
    }

    public Course addSection(@NonNull String sectionTypeId, @NonNull Section s) {
        SectionType st = sectionTypes.computeIfAbsent(sectionTypeId, x -> SectionType.of(this.school, this.term, x));
        st.addSection(s);
        return this;
    }

    public Course addSections(@NonNull SectionType sectionType) {
        sectionType.getSections().forEach(x -> this.addSection(sectionType.getCode(), x));
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("* Course: ").append(this.name)
                .append(" [").append(this.code).append("]");

        sb.append("\n* Department: ").append(this.department);
        sb.append("\n* Term: ").append(this.term);
        this.getCredits().ifPresent(x -> sb.append("\n* Credits: ").append(x));

        if (!this.crossListings.isEmpty()) {
            sb.append("\n* Cross-listings: ")
                    .append(this.crossListings.keySet().stream()
                            .sorted().collect(Collectors.joining(", ")));
        }
        if (!this.prerequisites.isEmpty()) {
            sb.append("\n* Pre-requisites: ")
                    .append(this.prerequisites.keySet().stream()
                            .sorted().collect(Collectors.joining(", ")));
        }
        if (!this.antirequisites.isEmpty()) {
            sb.append("\n* Anti-requisites: ")
                    .append(this.antirequisites.keySet().stream()
                            .sorted().collect(Collectors.joining(", ")));
        }
        if (!this.corequisites.isEmpty()) {
            sb.append("\n* Co-requisites: ")
                    .append(this.corequisites.keySet().stream()
                            .sorted().collect(Collectors.joining(", ")));
        }

        if (!this.notes.isEmpty()) {
            sb.append("\n* Notes:\n");
            this.notes.forEach(x -> sb.append('\n').append(StringUtilities.indent(1, x)));
            sb.append('\n');
        }

        sb.append("\n* Sections:");

        if (this.sectionTypes.isEmpty()) {
            sb.append("\n\n").append(I).append("NONE");
        } else {
            this.sectionTypes.values().forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(1, x.toString())));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull Course that) {
        if (!this.department.equals(that.department)) {
            return this.department.compareTo(that.department);
        }
        if (!this.code.equals(that.code)) {
            return this.code.compareTo(that.code);
        }
        if (!this.name.equals(that.name)) {
            return this.name.compareTo(that.name);
        }
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getDeltaId() {
        return this.getUniqueId();
    }

    @Override
    public StructureDelta findDifferences(@NonNull Course that) {

        if (!this.getUniqueId().equals(that.getUniqueId()) || !Objects.equals(this.school, that.school)) {
            throw new IllegalArgumentException("Courses are not related: \"" + this.getUniqueId()
                    + "\" and \"" + that.getUniqueId() + "\"");
        }

        final StructureDelta delta = StructureDelta.of(PropertyType.COURSE, this);

        delta.addValueIfChanged(PropertyType.NAME, this.name, that.name);
        delta.addValueIfChanged(PropertyType.DESCRIPTION, this.description, that.description);
        delta.addValueIfChanged(PropertyType.CREDITS, this.credits, that.credits);

        // Add added notes.
        that.notes.stream()
                .filter(x -> !this.notes.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.NOTE, x));

        // Add removed notes.
        this.notes.stream()
                .filter(x -> !that.notes.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.NOTE, x));

        recordCourseRelationDiff(delta, PropertyType.CROSS_LISTING, this.crossListings, that.crossListings);
        recordCourseRelationDiff(delta, PropertyType.PRE_REQUISITE, this.prerequisites, that.prerequisites);
        recordCourseRelationDiff(delta, PropertyType.ANTI_REQUISITE, this.antirequisites, that.antirequisites);
        recordCourseRelationDiff(delta, PropertyType.CO_REQUISITE, this.corequisites, that.corequisites);

        Set<String> sectionTypeKeys = new HashSet<>(this.sectionTypes.keySet());
        sectionTypeKeys.addAll(that.sectionTypes.keySet());

        // Add added section types.
        sectionTypeKeys.stream()
                .filter(x -> !this.sectionTypes.containsKey(x) && that.sectionTypes.containsKey(x))
                .forEach(x -> delta.addAdded(PropertyType.SECTION_TYPE, that.sectionTypes.get(x)));

        // Add removed section types.
        sectionTypeKeys.stream()
                .filter(x -> this.sectionTypes.containsKey(x) && !that.sectionTypes.containsKey(x))
                .forEach(x -> delta.addRemoved(PropertyType.SECTION_TYPE, this.sectionTypes.get(x)));

        // Add changed section types.
        sectionTypeKeys.stream()
                .filter(x -> this.sectionTypes.containsKey(x) && that.sectionTypes.containsKey(x))
                .filter(x -> !this.sectionTypes.get(x).equals(that.sectionTypes.get(x)))
                .forEach(x -> delta.addSubstructureChange(
                        this.sectionTypes.get(x).findDifferences(that.sectionTypes.get(x)))
                );

        return delta;
    }

    private static void recordCourseRelationDiff(StructureDelta delta, PropertyType propertyType,
                                                 Map<String, Course> thisListing,
                                                 Map<String, Course> thatListing) {

        Set<String> removed = thisListing.keySet();
        Set<String> added = thatListing.keySet();
        Set<String> overlap = new HashSet<>(added);
        overlap.retainAll(removed);

        added.stream().filter(x -> !overlap.contains(x))
                .forEach(x -> delta.addAdded(propertyType, thatListing.get(x)));
        removed.stream().filter(x -> !overlap.contains(x))
                .forEach(x -> delta.addRemoved(propertyType, thisListing.get(x)));
    }
}
