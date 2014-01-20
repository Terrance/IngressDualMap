package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.support.v4.app.NotificationCompat.Builder;

public class Portal {

    public static ArrayList<Portal> PORTALS = new ArrayList<Portal>(Arrays.asList(new Portal[]{
        new Portal("Jose G Artigas", -34.524003, -56.28161),
        new Portal("Esculture Iron In San Fernando Maldonado", -34.913484, -54.939271),
        new Portal("Cupid's Span", 37.791541, -122.390014),
        new Portal("Pirámide de Mayo", -34.608403, -58.372164),
        new Portal("Sintra", 38.797588, -9.390053),
        new Portal("Downing College Gate", 52.201493, 0.125232),
        new Portal("Mait Rob library", 52.201439, 0.124709),
        new Portal("Polar Harpoon Gun", 52.198347, 0.125964),
        new Portal("202 Downing College", 52.198252, 0.123689),
        new Portal("Gurdon Institute", 52.198677, 0.123044),
        new Portal("Department of Engineering", 52.198675, 0.121641),
        new Portal("Department of Anatomy  ", 52.20168, 0.122262),
        new Portal("University Chemical Laboratori", 52.198039, 0.124886),
        new Portal("Downing College Back Gate", 52.198577, 0.125462),
        new Portal("Engineering Department Fountain", 52.19806, 0.120535),
        new Portal("Youth - Lux Perpetua Luceat eis", 52.198537, 0.126375),
        new Portal("Unicorn ", 52.197938, 0.122446),
        new Portal("Kenny Gate, Downing College", 52.200376, 0.122086),
        new Portal("Lensfield Hotel", 52.197974, 0.123561),
        new Portal("CB1", 52.201128, 0.13428),
        new Portal("St. Barnabas' Church", 52.19953, 0.138045),
        new Portal("Zipcar-Gresham Road", 52.198703, 0.131127),
        new Portal("St. Paul's Church Cambridge", 52.197498, 0.129137),
        new Portal("Prancing Pony", 52.199587, 0.127232),
        new Portal("The Prince Regent", 52.199927, 0.126854),
        new Portal("Our Lady and English Martyrs Church", 52.199022, 0.12739),
        new Portal("Sancton Wood Unicorn", 52.198147, 0.130207),
        new Portal("Fabloous Toilets", 52.200045, 0.128138),
        new Portal("J.M. Keynes' house", 52.198945, 0.130123),
        new Portal("Betty Rea's Swimmers", 52.201944, 0.132222),
        new Portal("Skate Park", 52.202182, 0.131224),
        new Portal("Zion Baptist Church", 52.203672, 0.132548),
        new Portal("Mill Road Cemetery", 52.202729, 0.137438),
        new Portal("Cambridge University Botanic Garden", 52.194897, 0.122601),
        new Portal("Parker's Piece Football Bin", 52.201788, 0.125451),
        new Portal("The Regal", 52.20296, 0.123895),
        new Portal("Clock Tower", 52.203764, 0.124214),
        new Portal("The Fountain Pub", 52.202032, 0.124725),
        new Portal("Hobbs Pavilion", 52.202647, 0.126008),
        new Portal("Emma in Fall", 52.203254, 0.124203),
        new Portal("Reality Checkpoint", 52.202171, 0.128176),
        new Portal("Jester", 52.204254, 0.125241),
        new Portal("Old Police Station", 52.202487, 0.124293),
        new Portal("St Andrew's Church", 52.202699, 0.124088),
        new Portal("St. Radegund Public House", 52.207588, 0.126652),
        new Portal("The Grafton Centre ", 52.206762, 0.13169),
        new Portal("Eden Chapel", 52.206747, 0.129219),
        new Portal("Unitarian Church", 52.205133, 0.125977),
        new Portal("The Elm Tree", 52.205032, 0.129428),
        new Portal("Charles Humphrey Plaque", 52.207784, 0.129046),
        new Portal("Wesley Methodist Angel", 52.207515, 0.126938),
        new Portal("Clarendon Arms", 52.20467, 0.127623),
        new Portal("Kinetic Sculpture", 52.206259, 0.135055),
        new Portal("Cambridge Crown Court", 52.206918, 0.136581),
        new Portal("Cobble Yard Post Office", 52.20759, 0.134376),
        new Portal("Cambridge Buddhist Centre", 52.208144, 0.134689),
        new Portal("CB2 Bistro", 52.205024, 0.134318),
        new Portal("Christ Church Cambridge ", 52.207991, 0.133369),
        new Portal("Clare Bridge", 52.204974, 0.113817),
        new Portal("Kings College Xu Zhimo Memorial", 52.203699, 0.113855),
        new Portal("Confucius", 52.204924, 0.112552),
        new Portal("Clare Fellows Garden", 52.204992, 0.112918),
        new Portal("Bigger Bite", 52.201882, 0.109198),
        new Portal("Trinity Hall Front Court", 52.205712, 0.115735),
        new Portal("Reclining Chinese Nude Chef with Wok", 52.205065, 0.110001),
        new Portal("Faculty of History", 52.201876, 0.10889),
        new Portal("New Court St John's", 52.20831, 0.114359),
        new Portal("Bridge of Sighs, Cambridge", 52.20842, 0.115774),
        new Portal("University Library Gates", 52.205555, 0.108888),
        new Portal("Cambridge, England: Clare Coll", 52.204796, 0.11078),
        new Portal("Clare College Gate", 52.205074, 0.1156),
        new Portal("Cambridge University Library", 52.205051, 0.10847),
        new Portal("The Maypole", 52.209642, 0.119436),
        new Portal("Fosters Bank", 52.20545, 0.121377),
        new Portal("Garden Of Unearthly Delights", 52.206196, 0.118595),
        new Portal("St. John's Side Entrance", 52.208694, 0.11787),
        new Portal("King's Courtyard Statue", 52.204287, 0.11659),
        new Portal("Senate House", 52.205227, 0.117728),
        new Portal("St. Catharines College", 52.202998, 0.117301),
        new Portal("Church of St. Edward King and Martyr", 52.204641, 0.118747),
        new Portal("Indigo Cafe", 52.202802, 0.116016),
        new Portal("Market Square Fountain", 52.205356, 0.119056),
        new Portal("A Time And Place For Marksmanship", 52.205257, 0.120763),
        new Portal("Kings College Chapel", 52.20432, 0.117168),
        new Portal("Trinity Lane,Cambridge,England", 52.206302, 0.117651),
        new Portal("Talos Man of Bronze", 52.204747, 0.119877),
        new Portal("The Anchor", 52.20196, 0.115722),
        new Portal("Holy Trinity War Memorial", 52.205853, 0.120715),
        new Portal("The Round Church", 52.208433, 0.118868),
        new Portal("Church of St. Clement", 52.209058, 0.117833),
        new Portal("Trinity Street Angels", 52.207306, 0.118067),
        new Portal("Mond Building", 52.203289, 0.11937),
        new Portal("The Eagle", 52.203922, 0.118064),
        new Portal("Statue of Dr. Stephen Perse", 52.206163, 0.117958),
        new Portal("Trinity College ,Cambridge", 52.206433, 0.116665),
        new Portal("Corpus Christi College", 52.202853, 0.117788),
        new Portal("Holy Trinity Church", 52.205891, 0.120124),
        new Portal("The Corpus Clock", 52.203767, 0.117539),
        new Portal("Sydney Sussex Obelisk", 52.206746, 0.120966),
        new Portal("Corn Exchange", 52.204403, 0.119576),
        new Portal("Trinity Great Court", 52.206949, 0.11688),
        new Portal("Trinity College Gate", 52.207173, 0.117858),
        new Portal("Snowy Farr Memorial Sculpture", 52.205067, 0.119443),
        new Portal("St. John's College Statue", 52.207703, 0.117965),
        new Portal("Mathematical Bridge", 52.202177, 0.115022),
        new Portal("Henry VIII, chair leg in hand", 52.207132, 0.11763),
        new Portal("soldier : Suffolk Regiment", 52.205332, 0.118587),
        new Portal("Stephen Perse Marker", 52.202674, 0.119195),
        new Portal("Steven Hawking's room", 52.205745, 0.117195),
        new Portal("The Pitt Building", 52.202087, 0.117819),
        new Portal("Cambridge Stone Sculpture", 52.204488, 0.120355),
        new Portal("Pair of Bears ", 52.202832, 0.12216),
        new Portal("Millers Yard", 52.20158, 0.116767),
        new Portal("Downing Site Sundial", 52.202593, 0.121743),
        new Portal("United Reform Church", 52.201416, 0.118314),
        new Portal("Peterhouse College", 52.201001, 0.118781),
        new Portal("The Mill", 52.20141, 0.116078),
        new Portal("Peterhouse College ", 52.200872, 0.118465),
        new Portal("Marine Biology Skeleton", 52.203218, 0.120442),
        new Portal("Humphry Museum", 52.203075, 0.12149),
        new Portal("Wooly Mammoth ", 52.203109, 0.122145),
        new Portal("Sundial", 52.201569, 0.121165),
        new Portal("Saint Columba's Church", 52.203268, 0.122659),
        new Portal("Jesus College Horse", 52.209132, 0.12337),
        new Portal("All Saints", 52.208249, 0.12355),
        new Portal("Jesus College, Cambridge", 52.209666, 0.123746),
        new Portal("Clowns Italian Cafe", 52.207359, 0.1235),
        new Portal("Jesus College Dinosaurs", 52.208493, 0.125385),
        new Portal("Champion of the Thames ", 52.207314, 0.124419),
        new Portal("The Alma", 52.196008, 0.125384),
        new Portal("Lilian Bench in Botanic Garden", 52.194731, 0.123994),
        new Portal("John Stevens Henslow", 52.194676, 0.125085),
        new Portal("50th Anniversary Hidden Rock Garden Bench", 52.194147, 0.125497),
        new Portal("Cambridge Gardens Greenhouse", 52.194297, 0.12637),
        new Portal("Dragon Statues", 52.196208, 0.125912),
        new Portal("The Panton Arms", 52.196341, 0.124884),
        new Portal("Peter's Seat", 52.194154, 0.123906),
        new Portal("Henry and Margaret Chair", 52.19447, 0.125627),
        new Portal("Cacti from the Arid Lands", 52.194283, 0.126753),
        new Portal("The Violet Burrows Bench", 52.19468, 0.130462),
        new Portal("War Memorial in Cambridge", 52.194979, 0.131169),
        new Portal("Lilian M. Mudge Bench", 52.194412, 0.130221),
        new Portal("Kett House Tree", 52.194842, 0.131599),
        new Portal("Flying Pig", 52.194472, 0.131756),
        new Portal("Jagadis Chandra Bose Statue", 52.206537, 0.122947),
        new Portal("Christ's Pieces Gazebo", 52.206241, 0.124731),
        new Portal("Christ's College Gate", 52.205296, 0.121927),
        new Portal("Church of St. Giles", 52.210978, 0.115008),
        new Portal("Pepys Library", 52.210405, 0.116134),
        new Portal("Helix", 52.21021, 0.118075),
        new Portal("Gwen Raverat Plaque", 52.200605, 0.113194),
        new Portal("Darwin College", 52.200775, 0.113569),
        new Portal("City Map, Queen's Road", 52.201646, 0.113109),
        new Portal("Faculty of Classics Museum", 52.200675, 0.110447),
        new Portal("Palaeolithic Monolith", 52.200678, 0.110836),
        new Portal("The Fitzwilliam Museum", 52.199936, 0.120196),
        new Portal("Peterhouse's Park", 52.198195, 0.118727),
        new Portal("The Hand", 52.200178, 0.119997),
        new Portal("Doubletree by Hilton", 52.200086, 0.115943),
        new Portal("Statues at Fitzwilliam Museum", 52.200321, 0.119719),
        new Portal("Ridley Hall", 52.199929, 0.111489),
        new Portal("Newnham College", 52.199607, 0.10868),
        new Portal("The Granta Pub", 52.199644, 0.113686),
        new Portal("The Red Bull", 52.196528, 0.108768),
        new Portal("Law Faculty Cambridge", 52.201594, 0.109002),
        new Portal("Sidgwick", 52.201284, 0.109341),
        new Portal("Midsummer House", 52.212029, 0.128433),
        new Portal("Westminster College Gate", 52.210198, 0.111898),
        new Portal("Westminster College", 52.210507, 0.112003),
        new Portal("Honey Hill", 52.210578, 0.113482),
        new Portal("Eagle @ west gate St John's", 52.208816, 0.111268),
        new Portal("Microsoft's Building Art", 52.194627, 0.134887),
        new Portal("Longest Covered Footbridge", 52.196111, 0.137777),
        new Portal("Cambridge Railway station", 52.194191, 0.137229),
        new Portal("1911 Sculpture", 52.195429, 0.135046),
        new Portal("Welcome to CB1", 52.198693, 0.14103),
        new Portal("Bharat Bhavan", 52.199286, 0.139907),
        new Portal("The Earl of Beaconsfield", 52.198393, 0.142116),
        new Portal("Selwyn College Plodge", 52.201007, 0.105527),
        new Portal("Midsummer Bridge", 52.210268, 0.134417),
        new Portal("The Brunswick Restaurant & Bar", 52.208488, 0.136046),
        new Portal("Grafton Roundabout", 52.208481, 0.137615),
        new Portal("Space Dialogue", 52.21301, 0.109859),
        new Portal("Castle Mound", 52.211965, 0.114687),
        new Portal("Castle Street Methodist Church ", 52.21151, 0.113788),
        new Portal("3 Houses of Blackfriars Priory", 52.21381, 0.109265),
        new Portal("Murray Edwards College", 52.214178, 0.109337),
        new Portal("Tony's Trough", 52.214159, 0.124383),
        new Portal("Cambridge '99 Rowing Club", 52.211571, 0.129722),
        new Portal("Saint Luke's Church", 52.215781, 0.115862),
        new Portal("Giant Anchor", 52.21127, 0.13684),
        new Portal("Zipcar-E. Hertford Street", 52.212764, 0.117518),
        new Portal("The Geldart Pub", 52.203697, 0.142268),
        new Portal("The Alexandra Arms", 52.20385, 0.139212),
        new Portal("Sturton Methodist Church", 52.20257, 0.141006),
        new Portal("Pointing Figure With a Child (1966)", 52.212087, 0.103248),
        new Portal("Centre For Mathematical Sciences, University Of Cambridge", 52.210026, 0.102558),
        new Portal("Three Figures (1970)", 52.212007, 0.102774),
        new Portal("Flight (1981)", 52.2123, 0.102662),
        new Portal("Diagram of an Object, Dhruva Mistry", 52.212766, 0.103966),
        new Portal("Clare Hall Sculpture", 52.204201, 0.104564),
        new Portal("Philip de Koning \"Sailing to the Future\"", 52.205196, 0.103834),
        new Portal("Statues, Robinson College", 52.205133, 0.104946),
        new Portal("Finback", 52.204654, 0.105203),
        new Portal("Robinson College Chapel", 52.204677, 0.105473),
        new Portal("Punk Postbox ", 52.21096, 0.13871),
        new Portal("Casey's Yard Memorial", 52.208775, 0.139512),
        new Portal("Cellarer's Checker", 52.209543, 0.139037),
        new Portal("'Genesis' by John Robinson", 52.209385, 0.10292),
        new Portal("Fred", 52.201117, 0.101484),
        new Portal("Leckhampton House", 52.201568, 0.101062),
        new Portal("George Tomphson Building ", 52.201323, 0.100541),
        new Portal("Wolfson Statue", 52.198619, 0.101753),
        new Portal("Zipcar-Barton Road", 52.197842, 0.100913),
        new Portal("Wolfson Bell", 52.198215, 0.100133),
        new Portal("Wolfson Flag Pole", 52.198186, 0.100828),
        new Portal("Wolfson College Clock", 52.198445, 0.101658),
        new Portal("Swift Tower", 52.213888, 0.1425),
        new Portal("Newnham Post Office", 52.195958, 0.109652),
        new Portal("St Mark's Newnham", 52.196463, 0.108243),
        new Portal("The Remembered", 52.210032, 0.144128),
        new Portal("Beehive Honeycomb", 52.206048, 0.143709),
        new Portal("My Little Pony", 52.198151, 0.142977),
        new Portal("Millroad Baptist Church", 52.197906, 0.143205),
        new Portal("Wychfield", 52.21645, 0.104294),
        new Portal("The Møller Centre ", 52.214098, 0.098921),
        new Portal("Peg Statue", 52.2137, 0.101799),
        new Portal("Four Square Walk Through", 52.212932, 0.102045),
        new Portal("Electrical Engineering Division ", 52.210407, 0.092895),
        new Portal("Zipcar-Charles Babbage Road", 52.209552, 0.08907),
        new Portal("Hauser Forum", 52.209226, 0.090375),
        new Portal("Whittle Labs", 52.211965, 0.092186),
        new Portal("William Gates Building", 52.211188, 0.091488),
        new Portal("Computer Laboratory Bike Stand", 52.210948, 0.091254),
        new Portal("Northumberland Telescope", 52.214273, 0.094383),
        new Portal("Sir Winston Churchill Chapel ", 52.21425, 0.09699),
        new Portal("Institute of Astronomy", 52.214471, 0.09532),
        new Portal("The Elements ", 52.21398, 0.097342),
        new Portal("Globe Sculpture", 52.211848, 0.099061),
        new Portal("Metallic Flames", 52.214335, 0.093676),
        new Portal("West Cambridge Wind Turbine", 52.211912, 0.093535),
        new Portal("Pulse  Sculpture", 52.214032, 0.094065),
        new Portal("36 Inch Telescope", 52.213434, 0.094109),
        new Portal("Fred Hoyle", 52.213924, 0.093561),
        new Portal("St Andrews Church", 52.21553, 0.1399),
        new Portal("Chesterton War Memorial", 52.215533, 0.139326),
        new Portal("Cambridge Museum of Technology", 52.212766, 0.143286),
        new Portal("Haymakers Chesterton", 52.217501, 0.139336),
        new Portal("Chesterton Towers", 52.217066, 0.139928),
        new Portal("Chesterton Bridge", 52.213492, 0.143261),
        new Portal("Zipcar-Fitzgerald Road", 52.215267, 0.141549),
        new Portal("Moon Sculpture", 52.213069, 0.102633),
        new Portal("Beast Alerted (1990)", 52.212475, 0.103781),
        new Portal("Silverwood   ", 52.207124, 0.144855),
        new Portal("Empress", 52.199063, 0.147127),
        new Portal("Hilary's Wholesale Mill Road", 52.197607, 0.144997),
        new Portal("Almeric Memorial Stone", 52.197107, 0.147246),
        new Portal("Zipcar-St. Phillip Road", 52.199142, 0.146641),
        new Portal("Kingdom Hall of Jehovah's Witnesses", 52.212745, 0.147268),
        new Portal("The Wrestlers", 52.210997, 0.146459),
        new Portal("Iron Spike", 52.210414, 0.147658),
        new Portal("Green Dragon Bridge", 52.217543, 0.145767),
        new Portal("Romsey Town Labour Club", 52.197091, 0.14859),
        new Portal("Harry Lintott Chair", 52.194095, 0.123145),
        new Portal("Engraved Artwork at the Garden Cafe", 52.193986, 0.128377)
    }));

    private String mName;
    private double mLatitude;
    private double mLongitude;
    private int mHacksRemaining;
    private float mDistance = Float.MAX_VALUE;
    private boolean mPinned = false;
    private Builder mNotificationBuilder = null;

    private Calendar mHackReset = null;
    private Calendar mBurnoutReset = null;

    public Portal(String name, double latitude, double longitude) {
        mName = name;
        mHacksRemaining = 4;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
    public float getDistance() {
        return mDistance;
    }
    public void setDistance(float distance) {
        mDistance = distance;
    }
    public int getHacksRemaining() {
        return mHacksRemaining;
    }
    public void setHacksRemaining(int hacksRemaining) {
        mHacksRemaining = hacksRemaining;
    }
    public boolean isPinned() {
        return mPinned;
    }
    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }
    public Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }
    public void setNotificationBuilder(Builder notificationBuilder) {
        mNotificationBuilder = notificationBuilder;
    }

    public int checkRunningHot() {
        if (mHackReset == null) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        long diff = mHackReset.getTimeInMillis() - now.getTimeInMillis();
        if (diff < 0) {
            mHackReset = null;
            return 0;
        } else {
            return Math.round(diff / 1000);
        }
    }
    public void setRunningHot() {
        mHackReset = Calendar.getInstance();
        mHackReset.set(Calendar.MINUTE, mHackReset.get(Calendar.MINUTE) + 5);
    }
    public int checkBurnedOut() {
        if (mBurnoutReset == null) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        long diff = mBurnoutReset.getTimeInMillis() - now.getTimeInMillis();
        if (diff < 0) {
            mHacksRemaining = 4;
            mBurnoutReset = null;
            return 0;
        } else {
            return Math.round(diff / 1000);
        }
    }
    public void setBurnedOut() {
        mBurnoutReset = Calendar.getInstance();
        mBurnoutReset.set(Calendar.HOUR, mBurnoutReset.get(Calendar.HOUR) + 4);
    }
    public void reset() {
        mHacksRemaining = 4;
        mHackReset = null;
        mBurnoutReset = null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[name=" + mName + ", lat=" + mLatitude + ", lng=" + mLongitude + ", hacks=" + mHacksRemaining + "]";
    }

}
