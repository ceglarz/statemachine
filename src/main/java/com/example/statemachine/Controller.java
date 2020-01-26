package com.example.statemachine;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    private StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;

    public Controller(StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    @PostMapping("api/order/{event}")
    public Order changeState(@RequestBody Order order, @PathVariable("event") String event) {
        StateMachine<OrderStates, OrderEvents> sm = build(order);
        sm.sendEvent(OrderEvents.valueOf(event));
        order.setStatus(sm.getState().getId().name());
        sm.stop();
        return order;
    }

    StateMachine<OrderStates, OrderEvents> build(Order order) {
        StateMachine<OrderStates, OrderEvents> sm = stateMachineFactory.getStateMachine();
        //sm.stop();
        rehydrateState(sm, sm.getExtendedState(), OrderStates.valueOf(order.getStatus()));
        sm.start();
        return sm;

    }


    private void rehydrateState(StateMachine<OrderStates, OrderEvents> newStateMachine, ExtendedState extendedState, OrderStates orderOrderStates) {
        newStateMachine.getStateMachineAccessor().doWithAllRegions(sma ->
                sma.resetStateMachine(new DefaultStateMachineContext<>(orderOrderStates, null, null, extendedState))
        );
    }
}
