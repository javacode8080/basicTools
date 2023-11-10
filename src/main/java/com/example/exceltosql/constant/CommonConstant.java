package com.example.exceltosql.constant;

public class CommonConstant {

    public static final String XLS = ".xls";

    public static final String XLSX = ".xlsx";

    public static final String HTTP = "http://";

    public static final String HTTPS = "https://";

    public static final String ADMIN = "admin";

    public static final String CORE = "@bic";

    public static final String OPSMGR = "@opsmgr";

    public static final String BIC_MARKING = "bic";
    public static final String INFOSIGHT = "infosight";

    public static final String NOT_PORTAL = "#opsmgr";

    public static final Character COMMA_CAHR = ',';

    public static final String COMMA = ",";

    public static final String COLON = ":";

    public static final String DOT = ".";

    public static final String ESCAPED_DOT = "\\.";

    public static final String SPACE = " ";

    public static final String UNDER_LINE = "_";

    public static final String ESCAPED_UNDER_LINE = "\\_";

    public static final String SLASH = "/";

    public static final String BACK_SLASH = "\\";

    public static final String DOUBLE_BACK_SLASH = "\\\\";

    public static final String SHARP = "#";

    public static final String JOINER = "-";

    public static final String WAVY_LINE = "~";

    public static final String QUOTE = "'";

    public static final String PERCENT = "%";

    public static final String EQUAL = "=";

    public static final String AT = "@";

    public static final String PLUS = "+";

    public static final String QUESTION = "?";

    public static final Integer NEGATIVE_ONE = -1;

    public static final Integer ZERO = 0;

    public static final Double ZERO_D = 0.0D;

    public static final Integer ONE = 1;

    public static final Double ONE_D = 1.0D;

    public static final String BIC_SPECIFIC_VERSION = "1.6.0";

    public static final Integer TWO = 2;

    public static final Integer THREE = 3;

    public static final Integer FOUR = 4;

    public static final Integer FIVE = 5;

    public static final Integer SIX = 6;

    public static final Integer SEVEN = 7;

    public static final Integer TEN = 10;

    public static final Integer FIFTEEN = 15;

    public static final Integer EIGHTEEN = 18;

    public static final Integer THIRTY = 30;

    public static final Integer THIRTY_SIX = 36;

    public static final Integer ONE_HUNDRED = 100;

    public static final Double ONE_HUNDRED_D = 100.0D;

    public static final Integer ONE_THOUSAND = 1000;

    public static final Double ONE_THOUSAND_D = 1000.0D;

    public static final Integer TEN_THOUSAND = 10000;

    public static final Integer ONE_HOUR_SEC = 3600;

    public static final Integer QUARTER_MIN_MIL = 15000;

    public static final Integer ONE_MIN_MIL = 60000;

    public static final Integer FIVE_MIN_MIL = 300000;

    public static final Integer HALF_HOUR_MIL = 1800000;

    public static final Integer ONE_HOUR_MIL = 3600000;

    public static final Integer ONE_DAY_MIL = 86400000;

    public static final Integer ONE_DAY_MIN = 1440;

    public static final Integer PAGE_MAX = 99999;

    public static final Integer VALID_PACK = 10240;

    public static final String AND_DEVICE = "&device=";

    public static final String PIC_PRE = "/pic";

    public static final String TOKEN = "Token";

    public static final String SECU_SID = "SecuSID";

    public static final String SECU_DK = "SecuDK";

    public static final String DEFAULT_AES_TYPE = "AES256/CBC/PKCS5Padding";

    public static final Integer DEFAULT_DH_PRIME_LENGTH = 1024;

    public static final String ZIP_SUFFIX = ".zip";

    public static final String WAR_SUFFIX = ".war";

    public static final String SQL_SUFFIX = ".sql";

    public static final String PG = "PostgreSQL";

    public static final String LINUX = "Linux";

    public static final String UPGRADE = "upgrade";

    public static final String COMPONENT_PACKAGE_INFO_PATH = "META-INF/packageinfo.xml";

    public static final String PRODUCT_PACKAGE_INFO_PATH = "Source/product/META-INF/packageinfo.xml";

    public static final String VERSION_INFO_EX_PATH = "META-INF/versioninfoex.xml";

    public static final String INSTALLATION_PATH = "META-INF/installation.xml";

    public static final String PROPERTIES_PATH = "META-INF/properties.xml";

    public static final String COMPONENT_DIR_PATH = "Source/install/components/";

    public static final String FRAMEWORK_DIR_PATH = "Source/framework/";

    public static final String COMPONENT_INSTALLDEPEND_PATH = "Source/install/installdepend.txt";

    public static final String PREDEFINED_SERVICE_PATH = "Source/install/predefinedservices.txt";

    public static final String RESOURCE_DIR_PATH = "Source/install/resource/";

    public static final String RESOURCE_PATH = "Source/resource/";

    public static final String DAC_DRIVE_PATCH = "dacDriverPack";

    public static final String IAC_DRIVE_PATCH = "iacDriverPack";

    public static final String LANGUAGE_PACK_PATCH = "languagePack";

    public static final String RESOURCE_PACK_PATCH = "resourcePack";

    public static final String SKIN_PACK_PATCH = "skinPack";

    public static final String CONF_PROPERTIES_PATH = "Source/product/conf/conf.properties";

    public static final Integer COMPONENT_TYPE = 1;

    public static final Integer MAX_PACKAGE_THREAD_COUNT = 5;

    public static final Integer WAIT_MINUTES = 1;

    public static final String PACK_VERSION_FORMAT = "yyyyMMddHHmmss";

    /**
     * 基线
     */
    public static final String PACKAGE_BASELINE = "BAS";

    /**
     * 子业务
     */
    public static final String PACKAGE_SUBSYSTEM = "SUB";

    /**
     * 定制
     */
    public static final String PACKAGE_CUSTOMIZED = "CTM";

    /**
     * 通用维护
     */
    public static final String PACKAGE_COMMON_MAINTAIN = "PTC";

    /**
     * 行业维护
     */
    public static final String PACKAGE_INDUSTRY_MAINTAIN = "PTI";

    public static final String INSTALLER_REALTIVE_PATH = "Installer";

    public static final String UPGRADER_REALTIVE_PATH = "Upgrader";

    public static final String MAINTAIN_REALTIVE_PATH = "Maintain";

    public static final String CONFIG_RELATIVE_PATH = "Config";

    public static final String ENCODING_FORMAT_UTF8 = "utf-8";

    public static final Integer DEFAULT_EXPIRED_DAYS = 30;

    public static final String OPSMGRCENTER = "opsMgrCenter_";

    public static final String PACKAGE = "package";

    public static final Integer WEBSOCKET_RETRY_TIMES = 5;

    public static final Integer WEBSOCKET_RETRY_SECONDS = 3;

    public static final String REGEX_CHINESE = "[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】|\\、|\\’|\\‘]";

    public static final String REGEX_DIGITAL = "^(-?[1-9]\\d*\\.?\\d*)|(-?0\\.\\d*[1-9])|(-?[0])|(-?[0]\\.\\d*)$";

    public static final String PATCHE_INCLUDE_TOOL = "补丁已内置工具";

    public static final String GBK = "GBK";
}
