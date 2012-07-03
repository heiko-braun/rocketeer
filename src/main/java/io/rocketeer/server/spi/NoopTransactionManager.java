package io.rocketeer.server.spi;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

/**
 * @author Heiko Braun
 * @date 7/2/12
 */
public class NoopTransactionManager implements TransactionManager {
    public void begin() throws NotSupportedException, SystemException {
        
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        
    }

    public int getStatus() throws SystemException {
        return 0;  
    }

    public Transaction getTransaction() throws SystemException {
        return new NoopTransaction();
    }

    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        
    }

    public void setTransactionTimeout(int i) throws SystemException {
        
    }

    public Transaction suspend() throws SystemException {
        return new NoopTransaction();
    }
    
    
    class NoopTransaction implements Transaction {
        public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
            
        }

        public boolean delistResource(XAResource xaResource, int i) throws IllegalStateException, SystemException {
            return false;  
        }

        public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
            return false;  
        }

        public int getStatus() throws SystemException {
            return 0;  
        }

        public void registerSynchronization(Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
            
        }

        public void rollback() throws IllegalStateException, SystemException {
            
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            
        }
    } 
    
}
