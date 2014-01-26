package to.uk.terrance.ingressdualmap;

public interface ILocationServiceFragment {
    public void setServiceConnection(LocationServiceWrap service);
    public void onServiceConnected();
    public void onServiceDisconnected();
}
