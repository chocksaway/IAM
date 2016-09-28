import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Code needs testing
 * Use with caution
 */
public class TestIAMRoleManagement  {

    public static void main( String[] args ) throws Exception {
        final TestIAMRoleManagement test =  new TestIAMRoleManagement();
        test.test();

    }

    private AmazonIdentityManagement getIamClient( ) {
        AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(new ProfileCredentialsProvider());
        return iam;
    }

    private void assertThat( boolean condition,
                             String message ){
        assert condition : message;
    }

    private void print( String text ) {
        System.out.println( text );
    }

    public void test() throws Exception{
        final AmazonIdentityManagement iam = getIamClient();

        final String namePrefix = UUID.randomUUID().toString() + "-";
        print( "Using prefix for test: " + namePrefix );

        final List<Runnable> cleanupTasks = new ArrayList<Runnable>();
        try {
            // Create role
            final String roleName = namePrefix + "RoleTest";
            print( "Creating role: " + roleName );
            iam.createRole( new CreateRoleRequest()
                    .withRoleName( roleName )
                    .withPath( "/path/" )
                    .withAssumeRolePolicyDocument(
                            "{\n" +
                                    "    \"Statement\": [ {\n" +
                                    "      \"Effect\": \"Allow\",\n" +
                                    "      \"Principal\": {\n" +
                                    "         \"Service\": [ \"ec2.amazonaws.com\" ]\n" +
                                    "      },\n" +
                                    "      \"Action\": [ \"sts:AssumeRole\" ]\n" +  // Mixed case action
                                    "    } ]\n" +
                                    "}" ) );
            cleanupTasks.add( new Runnable() {
                @Override
                public void run() {
                    print( "Deleting role: " + roleName );
                    iam.deleteRole( new DeleteRoleRequest()
                            .withRoleName( roleName ) );
                }
            } );

            // Get role
            print( "Getting role: " + roleName );
            {
                final GetRoleResult getRoleResult =
                        iam.getRole( new GetRoleRequest()
                                .withRoleName( roleName ) );
                assertThat( getRoleResult.getRole() != null, "Expected role" );
                assertThat( roleName.equals( getRoleResult.getRole().getRoleName() ), "Unexpected role name" );
                assertThat( "/path".equals( getRoleResult.getRole().getPath() ), "Unexpected role path" );
                assertThat( getRoleResult.getRole().getAssumeRolePolicyDocument() != null &&
                        getRoleResult.getRole().getAssumeRolePolicyDocument().contains("sts:AssumeRole"), "Expected assume role policy document" );
                assertThat( getRoleResult.getRole().getArn() != null, "Expected ARN" );
                assertThat( getRoleResult.getRole().getCreateDate() != null, "Expected created date" );
            }

            // List roles
            print( "Listing roles to verify role present: " + roleName );
            {
                final ListRolesResult listRolesResult = iam.listRoles();
                boolean foundRole = isRolePresent( roleName, listRolesResult.getRoles() );
                assertThat( foundRole, "Role not found in listing" );
            }

            // List roles with path
            print( "Listing roles by path to verify role present: " + roleName );
            {
                final ListRolesResult listRolesResult =
                        iam.listRoles( new ListRolesRequest().withPathPrefix( "/path" ) );
                boolean foundRole = isRolePresent( roleName, listRolesResult.getRoles() );
                assertThat( foundRole, "Role not found in listing for path" );
            }

            // List roles with non-matching path
            print( "Listing roles by path to verify role not present: " + roleName );
            {
                final ListRolesResult listRolesResult =
                        iam.listRoles( new ListRolesRequest()
                                .withPathPrefix( "/---should-not-match-any-profiles---4ad1c6d3-bfdd-4dc8-8754-523b6624ce15" ) );
                boolean foundRole = isRolePresent( roleName, listRolesResult.getRoles() );
                assertThat( !foundRole, "Role listed when path should not match" );
            }

            // Test UpdateAssumeRolePolicy
            print( "Updating assume role policy for role: " + roleName );
            iam.updateAssumeRolePolicy( new UpdateAssumeRolePolicyRequest()
                    .withRoleName( roleName )
                    .withPolicyDocument(
                            "{\n" +
                                    "    \"Statement\": [ {\n" +
                                    "      \"Effect\": \"Allow\",\n" +
                                    "      \"Principal\": {\n" +
                                    "         \"Service\": [ \"ec2.amazonaws.com\" ]\n" +
                                    "      },\n" +
                                    "      \"Action\": [ \"sts:assumerole\" ]\n" + // Lower case action
                                    "    } ]\n" +
                                    "}" ) );

            // Get role, verify assume role policy updated
            print( "Getting role to check for updated assume role policy: " + roleName );
            {
                final GetRoleResult getRoleResult =
                        iam.getRole( new GetRoleRequest()
                                .withRoleName( roleName ) );
                assertThat( getRoleResult.getRole() != null, "Expected role" );
                assertThat( roleName.equals( getRoleResult.getRole().getRoleName() ), "Unexpected role name" );
                assertThat( "/path".equals( getRoleResult.getRole().getPath() ), "Unexpected role path" );
                assertThat( getRoleResult.getRole().getAssumeRolePolicyDocument() != null &&
                        getRoleResult.getRole().getAssumeRolePolicyDocument().contains("sts:assumerole"), "Expected assume role policy document" );
                assertThat( getRoleResult.getRole().getArn() != null, "Expected ARN" );
                assertThat( getRoleResult.getRole().getCreateDate() != null, "Expected created date" );
            }

            // Add policy to role
            final String policyName = namePrefix + "RoleTest";
            print( "Adding policy: " + policyName + " to role: " + roleName );
            iam.putRolePolicy( new PutRolePolicyRequest()
                    .withRoleName( roleName )
                    .withPolicyName( policyName )
                    .withPolicyDocument(
                            "{\n" +
                                    "   \"Statement\":[{\n" +
                                    "      \"Effect\":\"Allow\",\n" +
                                    "      \"Action\":\"ec2:*\",\n" +
                                    "      \"Resource\":\"*\"\n" +
                                    "   }]\n" +
                                    "}" ) );
            cleanupTasks.add( new Runnable() {
                @Override
                public void run() {
                    print( "Removing policy: " + policyName + ", from role: " + roleName );
                    iam.deleteRolePolicy( new DeleteRolePolicyRequest().withRoleName( roleName ).withPolicyName( policyName ) );
                }
            } );

            // Get and validate role policy
            print( "Getting policy: " + policyName + ", for role: " + roleName );
            {
                final GetRolePolicyResult policyResult = iam.getRolePolicy( new GetRolePolicyRequest()
                        .withRoleName( roleName )
                        .withPolicyName( policyName ) );
                assertThat( roleName.equals( policyResult.getRoleName() ), "Unexpected role name: " + policyResult.getRoleName() );
                assertThat( policyName.equals( policyResult.getPolicyName() ), "Unexpected policy name: " + policyResult.getPolicyDocument() );
                assertThat( policyResult.getPolicyDocument() != null, "Expected policy document" );
            }

            // List role policies, ensure policy present
            print( "Listing policies for role:" + roleName + ", to check for policy: " + policyName );
            {
                final ListRolePoliciesResult listRolePoliciesResult =
                        iam.listRolePolicies( new ListRolePoliciesRequest()
                                .withRoleName( roleName ) );
                assertThat( listRolePoliciesResult.getPolicyNames() != null, "Expected policies" );
                assertThat( listRolePoliciesResult.getPolicyNames().size() == 1, "Expected one policy" );
                assertThat( policyName.equals( listRolePoliciesResult.getPolicyNames().get( 0 ) ), "Unexpected policy name" );
            }

            // Remove policy from role
            print( "Removing policy: " + policyName + ", from role: " + roleName );
            iam.deleteRolePolicy( new DeleteRolePolicyRequest().withRoleName( roleName ).withPolicyName( policyName ) );

            // List role policies, ensure policy removed
            print( "Listing policies for role:" + roleName + ", to check policy removed: " + policyName );
            {
                final ListRolePoliciesResult listRolePoliciesResult =
                        iam.listRolePolicies( new ListRolePoliciesRequest()
                                .withRoleName( roleName ) );
                assertThat( listRolePoliciesResult.getPolicyNames() == null || listRolePoliciesResult.getPolicyNames().isEmpty(), "Expected policies" );
            }

            // Delete role
            print( "Deleting role: " + roleName );
            iam.deleteRole( new DeleteRoleRequest().withRoleName( roleName ) );

            // List roles (check deleted)
            print( "Listing roles to check deletion of role: " + roleName  );
            {
                final ListRolesResult listRolesResult = iam.listRoles();
                boolean foundRole = isRolePresent( roleName, listRolesResult.getRoles() );
                assertThat( !foundRole, "Role found in listing after deletion" );
            }

            print( "Test complete" );
        } finally {
            // Attempt to clean up anything we created
            Collections.reverse( cleanupTasks );
            for ( final Runnable cleanupTask : cleanupTasks ) {
                try {
                    cleanupTask.run();
                } catch ( NoSuchEntityException e ) {
                    print( "Entity not found during cleanup." );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isRolePresent( final String roleName, final List<Role> roles ) {
        boolean foundRole = false;
        if ( roles != null ) for ( final Role role : roles ) {
            foundRole = foundRole || roleName.equals( role.getRoleName() );
        }
        return foundRole;
    }
}