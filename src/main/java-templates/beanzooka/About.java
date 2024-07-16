package beanzooka;

public final class About {

    private About() {
        // static class
    }

    public static String getName() {
        return "${project.name}";
    }

    public static String getVersion() {
        return "${project.version}";
    }
}