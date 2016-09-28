import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.SimulatePrincipalPolicyRequest;
import com.amazonaws.services.identitymanagement.model.SimulatePrincipalPolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;


public class PolicySimulatorWrapper {
    private SimulatePrincipalPolicyResult simPolRes;

    /**
     * Apply a policy and action to the IAM policy simulator
     *
     * @param policySourceArn
     *          policy arn
     * @param action
     *          resource action
     */
    PolicySimulatorWrapper(final String policySourceArn, final String action) {
        final Logger logger = LoggerFactory.getLogger(PolicySimulatorWrapper.class);

        SimulatePrincipalPolicyRequest simPolReq = new SimulatePrincipalPolicyRequest();

        final Collection<String> actionNames = new ArrayList<String>() {{
            add(action);
        }};



        simPolReq.setPolicySourceArn(policySourceArn); // this is the Role ARN
        simPolReq.setActionNames(actionNames);

        try {
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(new ProfileCredentialsProvider());
            simPolRes = iam.simulatePrincipalPolicy(simPolReq);
        }
        catch (Exception exception) {
            logger.debug(exception.getMessage(), exception);
        }
    }

    public SimulatePrincipalPolicyResult getSimulatePrincipalPolicyResult() {
        return simPolRes;
    }
}
