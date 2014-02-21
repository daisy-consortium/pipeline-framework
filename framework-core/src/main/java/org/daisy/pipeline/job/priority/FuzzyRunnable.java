package org.daisy.pipeline.job.priority;

import com.google.common.base.Function;
import com.google.common.primitives.Doubles;


public abstract class FuzzyRunnable extends PrioritizedRunnable{
        private InferenceEngine infereneceEngine;
        private double score;

        /**
         * @param infereneceEngine
         */
        public FuzzyRunnable(InferenceEngine infereneceEngine) {
                this.infereneceEngine = infereneceEngine;
        }

        /**
         * Returns the crisps values 
         */
        public abstract double[] getPriorities();

        /**
         * @return the infereneceEngine
         */
        public InferenceEngine getInfereneceEngine() {
                return infereneceEngine;
        }
       

        /**
         * Returns the score of this FuzzyRunnable
         *
         */
        public synchronized double getScore(){
                //Lazy score calcualtion and caching
                if(this.dirty){
                        double[] crispValues=Doubles.concat(new double[]{this.relativeWaitingTime},this.getPriorities());
                        this.score=this.infereneceEngine.getScore(crispValues);
                        this.dirty=false;
                }
                return this.score;
        }
}