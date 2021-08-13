package java.awt.event;

public class InputModifiersHelper {
    public static int getModifier(InputEvent event) {
        return event.modifiers;
    }

    public static boolean isMetaDown(InputEvent event) {
        return (getModifier(event) & InputEvent.META_MASK) != 0;
    }
}
