package to.uk.terrance.ingressdualmap;

public interface ILocationServiceFragment {

    /**
     * Called once on initialisation of the service wrapper.
     * @param service The service wrapper object to store for later use.
     */
    public void setServiceConnection(LocationServiceWrap service);

}
