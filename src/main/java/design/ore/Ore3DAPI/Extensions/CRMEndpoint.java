package design.ore.Ore3DAPI.Extensions;

import org.pf4j.ExtensionPoint;

import design.ore.Ore3DAPI.DataTypes.CRM.Customer;
import design.ore.Ore3DAPI.DataTypes.CRM.Employee;
import design.ore.Ore3DAPI.DataTypes.CRM.Transaction;
import design.ore.Ore3DAPI.DataTypes.Wrappers.UpdatePacket;

public interface CRMEndpoint extends ExtensionPoint
{	
	/*
	 * Retrieves the <code>Transaction</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Transaction</code>
	 * @return       the Transaction found at the provided location, or null if not found.
	 * @see          Transaction
	 */
	Transaction getTransaction(String id);
	
	/*
	 * Updates the <code>Transaction</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   transaction   the <code>Transaction</code> to be updated.
	 * @return                <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                   Transaction
	 */
	UpdatePacket updateTransaction(Transaction transaction);
	
	/*
	 * Creates a copy of the <code>Transaction</code> passed in.
	 * 
	 * @param   transaction   the <code>Transaction</code> to be duplicated.
	 * @return                <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                   Transaction
	 */
	UpdatePacket duplicateTransaction(Transaction transaction);
	
	/*
	 * Adds a new <code>Transaction</code> to the CRM.
	 * 
	 * @param   transaction   the <code>Transaction</code> to be updated.
	 * @return                the id of the newly created <code>Transaction</code>.
	 * @see                   Transaction
	 */
	String addTransaction(Transaction transaction);
	
	/*
	 * Marks a <code>Transaction</code> as locked. When another user loads a locked proposal, a warning will be given that they cannot save their work as someone already has it open.
	 * 
	 * @param   transaction   the <code>Transaction</code> to be locked.
	 * @param   lockedBy	  the name pf the person locking the order.
	 * @return                true if successful, otherwise false.
	 * @see                   Transaction
	 */
	boolean markTransactionAsLocked(Transaction transaction, String lockedBy);

	/*
	 * Retrieves the <code>Customer</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Customer</code>
	 * @return       the Customer found at the provided location, or null if not found.
	 * @see          Customer
	 */
	Customer getCustomer(String id);
	
	/*
	 * Updates the <code>Customer</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   id          a unique id to be handled by the CRM as a location for the <code>Customer</code>
	 * @param   customer    the <code>Customer</code> to be updated.
	 * @return              true if successful, otherwise false.
	 * @see                 Customer
	 */
	boolean updateCustomer(Customer transaction);
	
	/*
	 * Adds a new <code>Customer</code> to the CRM.
	 * 
	 * @param   customer    the <code>Customer</code> to be updated.
	 * @return              the id of the newly created <code>Customer</code>.
	 * @see                 Transaction
	 */
	String addCustomer(Customer customer);

	/*
	 * Retrieves the <code>Employee</code> stored under a given <code>id</code>.
	 * 
	 * @param   id   a unique id to be handled by the CRM as a location for the <code>Employee</code>
	 * @return       the Employee found at the provided location, or null if not found.
	 * @see          Employee
	 */
	Employee getEmployee(String id);

	/*
	 * Retrieves the <code>Employee</code> that correlates to the current user.
	 * 
	 * @return       the Employee associated with the current user, or null if not found.
	 * @see          Employee
	 */
	Employee getUsingEmployee();
	
	/*
	 * Updates the <code>Employee</code> associated with the id, to be handled by the CRM.
	 * 
	 * @param   id           a unique id to be handled by the CRM as a location for the <code>Employee</code>
	 * @param   employee     the <code>Employee</code> to be updated.
	 * @return               true if successful, otherwise false.
	 * @see                  Employee
	 */
	boolean updateEmployee(Employee employee);
	
	/*
	 * Adds a new <code>Employee</code> to the CRM.
	 * 
	 * @param   employee    the <code>Employee</code> to be updated.
	 * @return              the id of the newly created <code>Employee</code>.
	 * @see                 Employee
	 */
	String addEmployee(Employee employee);
}