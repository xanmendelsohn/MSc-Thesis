/*$$
 * Copyright (c) 2004, Repast Organization for Architecture and Design (ROAD)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the ROAD nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/
package uchicago.src.sim.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.util.Random;

/**
 * Tests randomization of list in generated basic actions.
 */
public class ScheduleListTest extends TestCase {
  
  private ArrayList list = new ArrayList();
  private Schedule schedule;
  private String order;
  private String newOrder;

  public ScheduleListTest(String name) {
    super(name);
  }

  public void setUp() {
    Random.createUniform();
    schedule = new Schedule();
    
    list = new ArrayList();
    
    for (int i = 0; i < 20; i++) {
      list.add(new ScheduleTestAgent(i, schedule));
    }

    order = new String();
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      order += a.getId() + " ";
    }

    //System.out.println("order: " + order);
    
  }

  private String getTicks() {
    return ((ScheduleTestAgent)list.get(0)).getTicks();
  }

  private void orderComp() {
    orderComp(10);
  }

  private void orderComp(int runTime) {
    runSchedule(runTime);
    newOrder = "";
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      newOrder += a.getId() + " ";
    }
  }

  public void testAtRnd() {
    schedule.scheduleActionAtRnd(3, list, "printId");
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 ".equals(getTicks()));
  }

  public void testAtRndLast() {
    schedule.scheduleActionAtRnd(4, list, "printId", Schedule.LAST);
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("4.0 ".equals(getTicks()));
  }

  public void testIntervalRnd() {
    schedule.scheduleActionAtIntervalRnd(3, list, "printId");
    orderComp(3);
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 6.0 9.0 ".equals(getTicks()));
  }

  public void testIntervalLastRnd() {
    schedule.scheduleActionAtIntervalRnd(3, list, "printId", Schedule.LAST);
    orderComp(3);
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("3.0 6.0 9.0 ".equals(getTicks()));
  }

   public void testBeginningRnd() {
    schedule.scheduleActionBeginningRnd(1, list, "printId");
    orderComp();
    assertTrue("shuffled list == unshuffled list", !(newOrder.equals(order)));
    //System.out.println("execution ticks: " + getTicks());
    assertTrue("1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 ".equals(getTicks()));
  }
    

  public void testOrder() {
    String newOrder = "";
    for (int i = 0; i < 20; i++) {
      ScheduleTestAgent a = (ScheduleTestAgent)list.get(i);
      newOrder += a.getId() + " ";
    }
    assertTrue("unshuffled != unshuffled", order.equals(newOrder));
  }

  public void runSchedule(int runTime) {
    //System.out.print("new order: ");
    for (int i = 0; i < runTime; i++) {
      schedule.execute();
    }
  }

  public void runSchedule() {
    runSchedule(10);
  }

  public static junit.framework.Test suite() {
    return new TestSuite(uchicago.src.sim.test.ScheduleListTest.class);
  }
}
