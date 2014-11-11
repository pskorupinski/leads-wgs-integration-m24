package leadsm24.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.checkNotNull;

import restx.common.Types;
import restx.*;
import restx.entity.*;
import restx.http.*;
import restx.factory.*;
import restx.security.*;
import static restx.security.Permissions.*;
import restx.description.*;
import restx.converters.MainStringConverter;
import static restx.common.MorePreconditions.checkPresent;

import javax.validation.Validator;
import static restx.validation.Validations.checkValid;

import java.io.IOException;
import java.io.PrintWriter;

@Component(priority = 0)

public class LEADSM24ResourceRouter extends RestxRouter {

    public LEADSM24ResourceRouter(
                    final LEADSM24Resource resource,
                    final EntityRequestBodyReaderRegistry readerRegistry,
                    final EntityResponseWriterRegistry writerRegistry,
                    final MainStringConverter converter,
                    final Validator validator,
                    final RestxSecurityManager securityManager) {
        super(
            "default", "LEADSM24ResourceRouter", new RestxRoute[] {
        new StdEntityRoute<Void, java.lang.String>("default#LEADSM24Resource#functionality1",
                readerRegistry.<Void>build(Void.class, Optional.<String>absent()),
                writerRegistry.<java.lang.String>build(java.lang.String.class, Optional.<String>absent()),
                new StdRestxRequestMatcher("GET", "/F1"),
                HttpStatus.OK, RestxLogLevel.DEFAULT) {
            @Override
            protected Optional<java.lang.String> doRoute(RestxRequest request, RestxRequestMatch match, Void body) throws IOException {
                securityManager.check(request, open());
                return Optional.of(resource.functionality1(
                        /* [QUERY] inputJSON */ checkPresent(request.getQueryParam("inputJSON"), "query param inputJSON is required")
                ));
            }

            @Override
            protected void describeOperation(OperationDescription operation) {
                super.describeOperation(operation);
                                OperationParameterDescription inputJSON = new OperationParameterDescription();
                inputJSON.name = "inputJSON";
                inputJSON.paramType = OperationParameterDescription.ParamType.query;
                inputJSON.dataType = "string";
                inputJSON.schemaKey = "";
                inputJSON.required = true;
                operation.parameters.add(inputJSON);


                operation.responseClass = "string";
                operation.inEntitySchemaKey = "";
                operation.outEntitySchemaKey = "";
                operation.sourceLocation = "leadsm24.rest.LEADSM24Resource#functionality1(java.lang.String)";
            }
        },
        new StdEntityRoute<Void, java.lang.String>("default#LEADSM24Resource#functionality1A",
                readerRegistry.<Void>build(Void.class, Optional.<String>absent()),
                writerRegistry.<java.lang.String>build(java.lang.String.class, Optional.<String>absent()),
                new StdRestxRequestMatcher("GET", "/F1A"),
                HttpStatus.OK, RestxLogLevel.DEFAULT) {
            @Override
            protected Optional<java.lang.String> doRoute(RestxRequest request, RestxRequestMatch match, Void body) throws IOException {
                securityManager.check(request, open());
                return Optional.of(resource.functionality1A(
                        /* [QUERY] inputJSON */ checkPresent(request.getQueryParam("inputJSON"), "query param inputJSON is required")
                ));
            }

            @Override
            protected void describeOperation(OperationDescription operation) {
                super.describeOperation(operation);
                                OperationParameterDescription inputJSON = new OperationParameterDescription();
                inputJSON.name = "inputJSON";
                inputJSON.paramType = OperationParameterDescription.ParamType.query;
                inputJSON.dataType = "string";
                inputJSON.schemaKey = "";
                inputJSON.required = true;
                operation.parameters.add(inputJSON);


                operation.responseClass = "string";
                operation.inEntitySchemaKey = "";
                operation.outEntitySchemaKey = "";
                operation.sourceLocation = "leadsm24.rest.LEADSM24Resource#functionality1A(java.lang.String)";
            }
        },
        new StdEntityRoute<Void, java.lang.String>("default#LEADSM24Resource#functionality2",
                readerRegistry.<Void>build(Void.class, Optional.<String>absent()),
                writerRegistry.<java.lang.String>build(java.lang.String.class, Optional.<String>absent()),
                new StdRestxRequestMatcher("GET", "/F2"),
                HttpStatus.OK, RestxLogLevel.DEFAULT) {
            @Override
            protected Optional<java.lang.String> doRoute(RestxRequest request, RestxRequestMatch match, Void body) throws IOException {
                securityManager.check(request, open());
                return Optional.of(resource.functionality2(
                        /* [QUERY] inputJSON */ checkPresent(request.getQueryParam("inputJSON"), "query param inputJSON is required")
                ));
            }

            @Override
            protected void describeOperation(OperationDescription operation) {
                super.describeOperation(operation);
                                OperationParameterDescription inputJSON = new OperationParameterDescription();
                inputJSON.name = "inputJSON";
                inputJSON.paramType = OperationParameterDescription.ParamType.query;
                inputJSON.dataType = "string";
                inputJSON.schemaKey = "";
                inputJSON.required = true;
                operation.parameters.add(inputJSON);


                operation.responseClass = "string";
                operation.inEntitySchemaKey = "";
                operation.outEntitySchemaKey = "";
                operation.sourceLocation = "leadsm24.rest.LEADSM24Resource#functionality2(java.lang.String)";
            }
        },
        });
    }

}
