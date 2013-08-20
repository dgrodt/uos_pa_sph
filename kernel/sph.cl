

kernel void sph_CalcNewP(
global float* body_P,
global float* body_rho,
const float rho,
const float m,
const float c,
const float gamma
)
{
	uint id = get_global_id(0);
	body_P[id] = (pow(body_rho[id]/rho, gamma)-1) * (rho * c * c) / gamma;
}

kernel void sph_CalcNewV(
global float4* body_Pos,
global float4* body_V,
const float DELTA_T,
const float EPSILON_SQUARED,
global float* body_P,
global float* body_rho,
const float m
)
{
	
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	
	float pi = 3.14159;
	float h = 0.001;
	float4 g = (float4)(0,-1000,0,0);
	float4 delta_V = (float4)0;
	float delta_rho = 0;
	
	/*
	for (int i = 0; i < N; i++) {
		float4 gradW = -2 * exp(-dot(body_Pos[id]-body_Pos[i], body_Pos[id]-body_Pos[i])/(h*h)) / (pow(pi,3/2) * pow(h,5)) * (body_Pos[id]-body_Pos[i]);
		delta_V += m * (body_P[i]/(body_rho[i] * body_rho[i]) + body_P[id]/(body_rho[id] * body_rho[id])) * gradW;	//viskosität
		delta_rho += m * dot(body_V[id] - body_V[i], gradW);
	}
	*/
	
	delta_V += g;
	
	float4 pos = body_Pos[id];
	float r[5];
	float4 b[5];
	b[0] = (float4)(pos.x, -1, pos.z, 0);
	b[1] = (float4)(pos.x, pos.y, -1, 0);
	b[2] = (float4)(pos.x, pos.y, 1, 0);
	b[3] = (float4)(-1, pos.y, pos.z, 0);
	b[4] = (float4)(1, pos.y, pos.z, 0);
	
	for (int i = 0; i < 5; i++) {
		r[i] = distance(pos, b[i]);

		if (r[i] < 0.1) {
			delta_V += (100 * (pown(0.1/r[i], 4) - pown(0.1/r[i], 2)) / (r[i] * r[i])) *  (pos - b[i]);
		}
	
	}
	/*
	for (int i = -20; i <= 20; i++) {
		for (int j = -20; j <= 20; j++) {
			float4 b = (float4)(i/(float)20, -1, j/(float)20, 0);
			float4 diff = body_Pos[id] - b;
			float r = distance(body_Pos[id], b);
			if (r < 0.1) {
				delta_V += (100 * (pown(0.1/r, 4) - pown(0.1/r, 2)) / (r * r)) * diff;
			}
		}
	}
	*/
	
	delta_V *= DELTA_T;
	delta_rho *= DELTA_T;
	
	body_V[id] += delta_V;
	body_rho[id] += delta_rho;
	
}


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

kernel void sph_CalcNewV_old(
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

kernel void sph_CalcNewPos(
global float4* body_Pos, 
global float4* body_V,
const float DELTA_T)
{
    uint id = get_global_id(0);
    body_Pos[id] += body_V[id] * DELTA_T;
}