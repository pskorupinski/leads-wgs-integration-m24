package leadsm24;

import restx.common.ConfigElement;
import restx.common.RestxConfig;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Provides;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import restx.security.*;
import restx.security.StdCORSAuthorizer.Builder;
import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

import java.nio.file.Paths;
import java.util.regex.Pattern;

@Module
public class AppModule {
    @Provides
    public SignatureKey signatureKey() {
         return new SignatureKey("252bde4b-e987-482b-afb6-5a5c0bda1cb4 LEADSM24 -9006119301756682739 leadsm24".getBytes(Charsets.UTF_8));
    }

    @Provides
    @Named("restx.admin.password")
    public String restxAdminPassword() {
        return "LEADSdemoWGS";
    }
    
    @Provides
    public CORSAuthorizer getAuthorizer() {
    	Builder builder = StdCORSAuthorizer.builder();
    	StdCORSAuthorizer authorizer = builder.build();
    	System.err.println(authorizer);
    	return authorizer;    	
    }

    @Provides
    public ConfigSupplier appConfigSupplier(ConfigLoader configLoader) {
        // Load settings.properties in leadsm24 package as a set of config entries
    	ConfigSupplier configSupplier = configLoader.fromResource("leadsm24/settings");
    	RestxConfig config = configSupplier.get();
    	for(ConfigElement element : config.elements())
    		System.setProperty(element.getKey(), element.getValue());
    	
        return configLoader.fromResource("leadsm24/settings");
    }

    @Provides
    public CredentialsStrategy credentialsStrategy() {
        return new BCryptCredentialsStrategy();
    }

    @Provides
    public BasicPrincipalAuthenticator basicPrincipalAuthenticator(
            SecuritySettings securitySettings, CredentialsStrategy credentialsStrategy,
            @Named("restx.admin.passwordHash") String defaultAdminPasswordHash, ObjectMapper mapper) {
        return new StdBasicPrincipalAuthenticator(new StdUserService<>(
                // use file based users repository.
                // Developer's note: prefer another storage mechanism for your users if you need real user management
                // and better perf
                new FileBasedUserRepository<>(
                        StdUser.class, // this is the class for the User objects, that you can get in your app code
                        // with RestxSession.current().getPrincipal().get()
                        // it can be a custom user class, it just need to be json deserializable
                        mapper,

                        // this is the default restx admin, useful to access the restx admin console.
                        // if one user with restx-admin role is defined in the repository, this default user won't be
                        // available anymore
                        new StdUser("admin", ImmutableSet.<String>of("*")),

                        // the path where users are stored
                        Paths.get("data/users.json"),

                        // the path where credentials are stored. isolating both is a good practice in terms of security
                        // it is strongly recommended to follow this approach even if you use your own repository
                        Paths.get("data/credentials.json"),

                        // tells that we want to reload the files dynamically if they are touched.
                        // this has a performance impact, if you know your users / credentials never change without a
                        // restart you can disable this to get better perfs
                        true),
                credentialsStrategy, defaultAdminPasswordHash),
                securitySettings);
    }
}
