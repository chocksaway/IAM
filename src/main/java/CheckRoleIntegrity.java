import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;


public class CheckRoleIntegrity {
    public boolean getRoleIntegrity() {
        final Logger logger = LoggerFactory.getLogger(CheckRoleIntegrity.class);

        final Collection<String> roles = new ArrayList<String>() {{
            add("int-notifications-dynamodb-s3-repl-role");
            add("lambda-dynamodb-execution-role");
            add("manual-bucket-milesd-in-manual-bucket-milesd-frankfurt-s3-repl-r");
            add("myNewRole");
            add("myNotVeryGoodRole");
        }};

        try {
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(new ProfileCredentialsProvider());

            ListRolesResult listRolesResult = iam.listRoles();

            for (Role eachRole : listRolesResult.getRoles()) {
                logger.info(eachRole.toString());
                if (!roles.contains(eachRole.getRoleName())) {
                    return false;
                }
            }
        }
        catch (Exception exception) {
            logger.debug(exception.getMessage(), exception);
            return false;
        }
        return true;
    }
}
