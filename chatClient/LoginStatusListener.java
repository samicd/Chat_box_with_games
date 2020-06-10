package chatClient;

public interface LoginStatusListener {

    /**
     * Should tell us when a user comes online
     * @param login
     */
    public void online(String login);


    /**
     * Should tell us whe a user goes offline
     * @param login
     */
    public void offline(String login);

}
