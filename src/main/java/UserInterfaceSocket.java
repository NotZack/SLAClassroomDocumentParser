import org.jetbrains.annotations.Contract;

/**
 * A socket used to communicate to and from the user interface.
 */
class UserInterfaceSocket {

    private CSVDataInteraction dataInteraction;

    @Contract(pure = true)
    UserInterfaceSocket(CSVDataInteraction dataInteraction) {
        this.dataInteraction = dataInteraction;
    }

    /**
     * Opens a new socket used to communicate with the interface.
     */
    void openSocket() {

    }
}
