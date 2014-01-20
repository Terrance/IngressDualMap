package to.uk.terrance.ingressdualmap;

import to.uk.terrance.ingressdualmap.Portal;

interface ILocationService {
    boolean isRunning();
    void setPortals(in java.util.List<Portal> portals);
    Portal getPortal(int i);
}
