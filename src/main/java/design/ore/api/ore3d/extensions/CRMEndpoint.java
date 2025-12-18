package design.ore.api.ore3d.extensions;

import java.util.List;
import java.util.function.Consumer;

import design.ore.api.ore3d.data.core.Transaction;
import design.ore.api.ore3d.data.crm.Customer;
import design.ore.api.ore3d.data.crm.employee.Employee;
import design.ore.api.ore3d.data.wrappers.UpdatePacket;

public interface CRMEndpoint
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
	 * @param   navigationID   the ID of the UI element calling this function.
	 * @param   transaction   the <code>Transaction</code> to be updated.
	 * @return                <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                   Transaction
	 */
	UpdatePacket updateTransaction(String navigationID, Transaction transaction);
	
	/*
	 * @deprecated Use {@link #duplicateTransaction(String, Transaction, boolean, Consumer<Transaction>)} instead.
	 * Creates a copy of the <code>Transaction</code> passed in.
	 * 
	 * @param   navigationID   the ID of the UI element calling this function.
	 * @param   transaction    the <code>Transaction</code> to be duplicated.
	 * @param   saveOriginal   true if the original transaction should be saved, otherwise false.
	 * @param   fromExpired    true if the original transaction was expired, otherwise false.
	 * @return                 <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                    Transaction
	 */
	default UpdatePacket duplicateTransaction(String navigationID, Transaction transaction, boolean saveOriginal, boolean fromExpired)
	{ return duplicateTransaction(navigationID, transaction, false, fromExpired, (tran) -> {}); }
	
	/*
	 * @deprecated Use {@link #duplicateTransaction(String, Transaction, Consumer<Transaction>)} instead.
	 * Creates a copy of the <code>Transaction</code> passed in.
	 * 
	 * @param   navigationID   the ID of the UI element calling this function.
	 * @param   transaction    the <code>Transaction</code> to be duplicated.
	 * @param   saveOriginal   true if the original transaction should be saved, otherwise false.
	 * @param   fromExpired    true if the original transaction was expired, otherwise false.
	 * @param   callback       Should be passed the duplicated transaction once it is created.
	 * @return                 <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                    Transaction
	 */
	default UpdatePacket duplicateTransaction(String navigationID, Transaction transaction, boolean saveOriginal, boolean fromExpired, Consumer<Transaction> callback)
	{ return duplicateTransaction(navigationID, transaction, fromExpired, (tran) -> {}); }
	
	/*
	 * Creates a copy of the <code>Transaction</code> passed in.
	 * 
	 * @param   navigationID   the ID of the UI element calling this function.
	 * @param   transaction    the <code>Transaction</code> to be duplicated.
	 * @param   saveOriginal   true if the original transaction should be saved, otherwise false.
	 * @param   callback       Should be passed the duplicated transaction once it is created.
	 * @return                 <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                    Transaction
	 */
	UpdatePacket duplicateTransaction(String navigationID, Transaction transaction, boolean fromExpired, Consumer<Transaction> callback);
	
	/*
	 * Generates work orders for each build UID contained in the <code>Transaction</code>.
	 * 
	 * @param   navigationID   the ID of the UI element calling this function.
	 * @param   transaction   the <code>Transaction</code> to be referenced.
	 * @param   buildUIDs     a list of build UIDs. Each UID will have its own work order generated. Builds not included in the list will be included as components of the work order, with no work order generated.
	 * @return                <code>UpdatePacket</code> containing tasks to perform the action.
	 * @see                   Transaction
	 */
	UpdatePacket generateWorkOrders(String navigationID, Transaction transaction, List<Integer> buildUIDs);
	
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