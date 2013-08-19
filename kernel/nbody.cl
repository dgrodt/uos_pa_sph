float4 bodyBodyInteraction(float4 pi, float4 pj, float4 ai, float EPSILON_SQUARED)  
{  
    float3 r;  

    r.x = pj.x - pi.x;  
    r.y = pj.y - pi.y;  
    r.z = pj.z - pi.z;  

    float distSqr = r.x * r.x + r.y * r.y + r.z * r.z + EPSILON_SQUARED;  

    float invDist = rsqrt(distSqr);
    float invDistCube =  invDist * invDist * invDist;
    //mass = 1
    float m = 1;
    float s = m * invDistCube;  

    ai.x += r.x * s;  
    ai.y += r.y * s;  
    ai.z += r.z * s;  
    return ai;  
}

kernel void nBody_CalcNewV(
global float4* body_Pos,
global float4* body_V,
const float DELTA_T,
const float EPSILON_SQUARED)
{
    uint id = get_global_id(0);
    uint N = get_global_size(0);

    float4 myPos = body_Pos[id];
    
    float4 ac = (float4)(0,0,0,0);
    
    for(uint i = 0; i < N; ++i)
    {  
        float4 otherPos = body_Pos[i];
        ac = bodyBodyInteraction(myPos, otherPos, ac, EPSILON_SQUARED);
    }
    
    float4 v = body_V[id];
    v += ac * DELTA_T;
    body_V[id] = v;
}

kernel void nBody_CalcNewPos(
global float4* body_Pos, 
global float4* body_V,
const float DELTA_T)
{
    uint id = get_global_id(0);
    body_Pos[id] += body_V[id] * DELTA_T;
}

kernel void passPositionOn(
global float4* body_Pos,
global float4* curveVertex_Pos,
const uint VERTICES_PER_CURVE)
{
    uint id = get_global_id(0);
    for(uint i = VERTICES_PER_CURVE-1 ; i > 0; --i){
  		curveVertex_Pos[id*VERTICES_PER_CURVE + i] = curveVertex_Pos[id*VERTICES_PER_CURVE + i-1];
  	}
    curveVertex_Pos[id*VERTICES_PER_CURVE] = body_Pos[id];
}

kernel void setTrailParticle(
global float4* trailParticle_Pos, 
global float*  trailParticle_S, 
global float4* trailParticle_Dir, 
global float4* curveVertex_Pos,   
const int TRAIL_PARTICLES_PER_CURVE,
const int VERTICES_PER_CURVE,   
const float dSC,
const float dSTP)
{
	uint id = get_global_id(0);
	for(uint i = 0; i < TRAIL_PARTICLES_PER_CURVE; ++i)
	{
		float sTP = trailParticle_S[id*TRAIL_PARTICLES_PER_CURVE+i];
		uint posPrevCVid = sTP * (VERTICES_PER_CURVE - 1);
		float sPrevCV = posPrevCVid * dSC;
		float sTPLocal = (sTP-sPrevCV)/dSC;
		float4 posPrevCV = curveVertex_Pos[id * VERTICES_PER_CURVE + posPrevCVid];
		float4 posNextCV = curveVertex_Pos[id * VERTICES_PER_CURVE + posPrevCVid + 1];
		
		trailParticle_Pos[id*TRAIL_PARTICLES_PER_CURVE+i] = posPrevCV * (1-sTPLocal) + posNextCV * sTPLocal;
		
		sTP = sTP + dSTP;
		if(sTP > 1)
		{
			sTP = sTP - 1;
		}
		trailParticle_S[id*TRAIL_PARTICLES_PER_CURVE+i] = sTP;
	
		trailParticle_Dir[id*TRAIL_PARTICLES_PER_CURVE+i] = (float4)(posNextCV - posPrevCV);
	}	
}