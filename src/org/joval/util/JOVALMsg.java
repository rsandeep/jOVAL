// Copyright (C) 2011 Patch-Service.com.  All rights reserved.

package org.joval.util;

import ch.qos.cal10n.LocaleData;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.BaseName;

/**
 * Uses cal10n to define localized messages for jOVAL.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
@BaseName("jovalmsg")
@LocaleData(
  defaultCharset="ASCII",
  value = { @Locale("en_US")
          }
)
public enum JOVALMsg {
    STATUS_AUTHMESSAGE,
    STATUS_EMPTY_FILE,
    STATUS_EMPTY_OBJECT,
    STATUS_NOT_FOUND,
    STATUS_PE_EMPTY,
    STATUS_PE_READ,
    STATUS_VARIABLE_CREATE,
    STATUS_VARIABLE_RECYCLE,
    STATUS_WINCRED_CREATE,
    STATUS_WINREG_CONNECT,
    STATUS_WINREG_DISCONNECT,
    STATUS_WINREG_KEYCLEAN,
    STATUS_WINREG_KEYCLOSED,
    STATUS_WINREG_REDIRECT,
    STATUS_WINREG_KEYDEREG,
    STATUS_WINREG_KEYREG,
    STATUS_WINREG_VALINSTANCE,
    STATUS_WINSMB_MAP,
    STATUS_SC_FILTER_ITEM,
    STATUS_SC_FILTER_VARIABLE,
    STATUS_TEST,
    STATUS_SESSIONTYPE,
    STATUS_WINDOWS_BITNESS,
    STATUS_RPMINFO_LIST,
    STATUS_RPMINFO_FULL,
    STATUS_RPMINFO_RPM,
    STATUS_SOLPKG_LIST,
    STATUS_SOLPKG_PKGINFO,
    STATUS_FS_RECURSE,
    STATUS_FS_SEARCH,
    STATUS_SMF,
    STATUS_SMF_SERVICE,
    STATUS_AD_DOMAIN_SKIP,
    STATUS_AD_DOMAIN_ADD,
    STATUS_AD_GROUP_SKIP,
    STATUS_UPN_CONVERT,
    STATUS_NAME_DOMAIN_ERR,
    STATUS_NAME_DOMAIN_OK,
    STATUS_UNIX_FILE,
    STATUS_OFFLINE,
    STATUS_WINREG_REDIRECT_OVERRIDE,
    STATUS_SSH_PROCESS_START,
    STATUS_SSH_PROCESS_END,
    ERROR_SESSIONTYPE,
    ERROR_ENGINE_STATE,
    ERROR_BAD_COMPONENT,
    ERROR_BAD_TIMEDIFFERENCE,
    ERROR_DEFINITION_NOID,
    ERROR_DEFINITIONS_BAD_FILE,
    ERROR_SCHEMATRON_VALIDATION,
    ERROR_DIRECTIVES_BAD_FILE,
    ERROR_EOF,
    ERROR_EOS,
    ERROR_EXTERNAL_VARIABLE_SOURCE,
    ERROR_FILE_GENERATE,
    ERROR_FILE_CLOSE,
    ERROR_FILE_DELETE,
    ERROR_FILEOBJECT_ITEMS,
    ERROR_PE,
    ERROR_PE_STRINGSTR_OVERFLOW,
    ERROR_FS_LOCALPATH,
    ERROR_FS_NULLPATH,
    ERROR_INSTANCE,
    ERROR_IO,
    ERROR_ADAPTER_MISSING,
    ERROR_ADAPTER_UNAVAILABLE,
    ERROR_MISSING_COMPONENT,
    ERROR_MISSING_VARIABLE,
    ERROR_MISSING_VARIABLE_COMPONENT,
    ERROR_NULL_PLUGIN,
    ERROR_OBJECT_ITEM_FIELD,
    ERROR_REF_DEFINITION,
    ERROR_REF_ITEM,
    ERROR_REF_OBJECT,
    ERROR_REF_STATE,
    ERROR_REF_TEST,
    ERROR_REF_VARIABLE,
    ERROR_SC_BAD_FILE,
    ERROR_RESULTS_BAD_FILE,
    ERROR_STATE_BAD,
    ERROR_OBJECT_MISSING,
    ERROR_STATE_MISSING,
    ERROR_TEST_NOOBJREF,
    ERROR_TEXTFILECONTENT_SPEC,
    ERROR_TESTEXCEPTION,
    ERROR_TIMESTAMP,
    ERROR_VARIABLES_BAD_FILE,
    ERROR_UNKNOWN_HOST,
    ERROR_UNIX_FLAVOR,
    ERROR_UNSUPPORTED_UNIX_FLAVOR,
    ERROR_UNSUPPORTED_OPERATOR,
    ERROR_UNSUPPORTED_OPERATION,
    ERROR_UNSUPPORTED_CHECK,
    ERROR_UNSUPPORTED_COMPONENT,
    ERROR_UNSUPPORTED_EXISTENCE,
    ERROR_UNSUPPORTED_STATE,
    ERROR_UNSUPPORTED_ENTITY,
    ERROR_UNSUPPORTED_DATATYPE,
    ERROR_UNSUPPORTED_BEHAVIOR,
    ERROR_OPERATION_DATATYPE,
    ERROR_UNEXPECTED_NODE,
    ERROR_VERSION_CLASS,
    ERROR_VERSION_STR,
    ERROR_PLUGIN_INTERFACE,
    ERROR_WINENV_NONSTR,
    ERROR_WINENV_SYSENV,
    ERROR_WINENV_SYSROOT,
    ERROR_WINENV_PROGRAMFILES,
    ERROR_WINENV_PROGRAMFILESX86,
    ERROR_WINENV_USRENV,
    ERROR_WINENV_VOLENV,
    ERROR_WINFILE_DEVCLASS,
    ERROR_WINFILE_LANGUAGE,
    ERROR_WINFILE_OWNER,
    ERROR_WINFILE_SPEC,
    ERROR_WINPE_BUFFERLEN,
    ERROR_WINPE_ILLEGALSECTION,
    ERROR_WINPE_LOCALERESOURCE,
    ERROR_WINPE_MAGIC,
    ERROR_WINPE_STRSTR0LEN,
    ERROR_WINPE_VSVKEY,
    ERROR_WINREG_CONNECT,
    ERROR_WINREG_CONVERSION,
    ERROR_WINREG_ENUMKEY,
    ERROR_WINREG_ENUMVAL,
    ERROR_WINREG_HIVE,
    ERROR_WINREG_HIVE_NAME,
    ERROR_WINREG_HIVE_OPEN,
    ERROR_WINREG_KEY,
    ERROR_WINREG_KEYCLOSE,
    ERROR_WINREG_ACCESS,
    ERROR_WINREG_MATCH,
    ERROR_PATTERN,
    ERROR_WINREG_QUERYVAL,
    ERROR_WINREG_REKEY,
    ERROR_WINREG_STATE,
    ERROR_WINREG_TYPE,
    ERROR_WINREG_VALUETOSTR,
    ERROR_WINREG_FLAVOR,
    ERROR_WINREG_DISCONNECT,
    ERROR_WINWMI_GENERAL,
    ERROR_WMI_CONNECT,
    ERROR_UNAME_OVERFLOW,
    ERROR_FAMILY_OVERFLOW,
    ERROR_STATE_EMPTY,
    ERROR_COMPONENT_EMPTY,
    ERROR_NO_ITEMS,
    ERROR_FILE_STREAM_CLOSE,
    ERROR_REFLECTION,
    ERROR_MISSING_PASSWORD,
    ERROR_AUTHENTICATION_FAILED,
    ERROR_DATATYPE_MISMATCH,
    ERROR_RPMINFO,
    ERROR_RPMINFO_SIGKEY,
    ERROR_SOLPKG,
    ERROR_SOLPKG_PKGINFO,
    ERROR_UNIX_FILE,
    ERROR_ILLEGAL_TIME,
    ERROR_TIME_PARSE,
    ERROR_BAD_FILEOBJECT,
    ERROR_SUBSTRING,
    ERROR_SET_COMPLEMENT,
    ERROR_SMF,
    ERROR_XML_XPATH,
    ERROR_XML_PARSE,
    ERROR_PRECACHE,
    ERROR_PRECACHE_LINE,
    ERROR_RESOLVE_VAR,
    ERROR_FMRI,
    ERROR_AD_DOMAIN_REQUIRED,
    ERROR_AD_DOMAIN_UNKNOWN,
    ERROR_AD_BAD_OU,
    ERROR_AD_INIT,
    ERROR_WMI_STR_CONVERSION,
    ERROR_SSH_UNEXPECTED_RESPONSE,
    ERROR_IOS_SHOW,
    ERROR_WINDOWS_BITNESS_INCOMPATIBLE,
    ERROR_EXCEPTION;
}
