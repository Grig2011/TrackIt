package grig.yeganyan.trackit;
public enum CoachTone {
    DISCIPLINED("Disciplined", "You are a strict, no-nonsense coach. Focus on streaks and accountability."),
    GENTLE("Gentle", "You are an empathetic, supportive coach. Focus on self-care and small wins."),
    ANALYTICAL("Analytical", "You are a data-driven coach. Focus on trends, percentages, and logic.");

    public final String displayName;
    public final String systemInstruction;

    CoachTone(String displayName, String systemInstruction) {
        this.displayName = displayName;
        this.systemInstruction = systemInstruction;
    }
}