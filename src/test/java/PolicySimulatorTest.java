import com.amazonaws.services.identitymanagement.model.SimulatePrincipalPolicyResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PolicySimulatorTest {
    @Test
    public void policySimulatorResultIsDenied() {
        PolicySimulatorWrapper policySimulator = new PolicySimulatorWrapper(
                "arn:aws:iam::457954557100:role/myNewRole",
                "s3:putObject");
        SimulatePrincipalPolicyResult polRes = policySimulator.getSimulatePrincipalPolicyResult();
        polRes.getEvaluationResults().get(0).getEvalDecision();
        assertEquals("implicitDeny", polRes.getEvaluationResults().get(0).getEvalDecision());
    }

    @Test
    public void policySimulatorResultIsAllowed() {
        PolicySimulatorWrapper policySimulator = new PolicySimulatorWrapper(
                "arn:aws:iam::457954557100:role/myNewRole",
                "s3:getObject");
        SimulatePrincipalPolicyResult polRes = policySimulator.getSimulatePrincipalPolicyResult();
        polRes.getEvaluationResults().get(0).getEvalDecision();
        assertEquals("allowed", polRes.getEvaluationResults().get(0).getEvalDecision());
    }

    @Test
    public void roleIntegrityTest() {
        CheckRoleIntegrity roleIntegrity = new CheckRoleIntegrity();
        assertTrue(roleIntegrity.getRoleIntegrity());


    }
}

